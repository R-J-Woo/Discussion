package com.discussion.ryu.repository;

import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.dto.discussion.SortType;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionPostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class DiscussionPostEsSearchRepositoryImpl implements DiscussionPostRepositoryCustom {

    /** POPULAR 정렬: agreeCount + disagreeCount 합산. 필드 미존재 시 0으로 처리 (null-safe). */
    private static final String POPULAR_SORT_SCRIPT =
            "(doc['agreeCount'].size() > 0 ? doc['agreeCount'].value : 0L)" +
            " + (doc['disagreeCount'].size() > 0 ? doc['disagreeCount'].value : 0L)";

    private final ElasticsearchOperations esOperations;
    private final DiscussionPostRepository discussionPostRepository;

    // ────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────

    @Override
    public Page<DiscussionPost> searchPosts(
            String keyword,
            DiscussionSearchDto.SearchType searchType,
            Pageable pageable,
            SortType sortType) {

        NativeQuery query = buildNativeQuery(keyword, searchType, sortType, pageable);
        SearchHits<DiscussionPostDocument> hits = esOperations.search(query, DiscussionPostDocument.class);

        List<Long> postIds = hits.stream()
                .map(hit -> hit.getContent().getPostId())
                .toList();

        if (postIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<DiscussionPost> ordered = toOrderedPosts(postIds);
        return new PageImpl<>(ordered, pageable, hits.getTotalHits());
    }

    // ────────────────────────────────────────────────────────────────
    // Query 조립
    // ────────────────────────────────────────────────────────────────

    private NativeQuery buildNativeQuery(String keyword,
                                         DiscussionSearchDto.SearchType searchType,
                                         SortType sortType,
                                         Pageable pageable) {
        return NativeQuery.builder()
                .withQuery(buildQuery(keyword, searchType))
                .withSort(buildSorts(keyword, sortType))
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .build();
    }

    /**
     * 키워드 유무에 따라 match/matchAll 쿼리를 선택하고,
     * soft-delete 문서(deletedAt 존재)를 mustNot 필터로 제외한다.
     */
    private Query buildQuery(String keyword, DiscussionSearchDto.SearchType searchType) {
        Query contentQuery = hasText(keyword)
                ? buildKeywordQuery(keyword, searchType)
                : Query.of(q -> q.matchAll(m -> m));

        return Query.of(q -> q
                .bool(b -> b
                        .must(contentQuery)
                        .mustNot(mn -> mn.exists(e -> e.field("deletedAt")))
                )
        );
    }

    /**
     * 검색 타입에 따라 단일 필드(match) 또는 다중 필드(multi_match) 쿼리를 반환한다.
     * title·content 필드는 nori 분석기가 적용되어 한국어 형태소 검색을 지원한다.
     */
    private Query buildKeywordQuery(String keyword, DiscussionSearchDto.SearchType searchType) {
        DiscussionSearchDto.SearchType type =
                searchType != null ? searchType : DiscussionSearchDto.SearchType.ALL;

        return switch (type) {
            case TITLE   -> Query.of(q -> q.match(m -> m.field("title").query(keyword)));
            case CONTENT -> Query.of(q -> q.match(m -> m.field("content").query(keyword)));
            default      -> Query.of(q -> q.multiMatch(m -> m.fields("title", "content").query(keyword)));
        };
    }

    // ────────────────────────────────────────────────────────────────
    // 정렬 조립
    // ────────────────────────────────────────────────────────────────

    /**
     * 키워드가 있으면 관련도 점수(_score)를 1순위로 추가한 뒤 sortType을 2순위로 붙인다.
     * 키워드가 없으면 sortType이 1순위가 된다.
     */
    private List<SortOptions> buildSorts(String keyword, SortType sortType) {
        List<SortOptions> sorts = new ArrayList<>();

        if (hasText(keyword)) {
            sorts.add(scoreSort());
        }

        sorts.add(fieldSort(sortType != null ? sortType : SortType.LATEST));
        return sorts;
    }

    private SortOptions scoreSort() {
        return SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc)));
    }

    private SortOptions fieldSort(SortType sortType) {
        return switch (sortType) {
            case MOST_AGREED   -> SortOptions.of(s -> s.field(f -> f.field("agreeCount").order(SortOrder.Desc)));
            case MOST_DISAGREED -> SortOptions.of(s -> s.field(f -> f.field("disagreeCount").order(SortOrder.Desc)));
            case POPULAR       -> SortOptions.of(s -> s
                    .script(sc -> sc
                            .type(ScriptSortType.Number)
                            .script(script -> script.source(POPULAR_SORT_SCRIPT).lang("painless"))
                            .order(SortOrder.Desc)));
            default            -> SortOptions.of(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
        };
    }

    // ────────────────────────────────────────────────────────────────
    // ES → JPA 매핑
    // ────────────────────────────────────────────────────────────────

    /**
     * ES에서 받은 postId 목록으로 JPA 엔티티를 조회하고 ES 결과 순서를 그대로 보존한다.
     * ES 인덱스에는 있지만 DB에 없는 ID가 있으면 경고 로그를 남긴다.
     */
    private List<DiscussionPost> toOrderedPosts(List<Long> postIds) {
        List<DiscussionPost> posts = discussionPostRepository.findAllByIdInWithAuthor(postIds);

        Set<Long> foundIds = posts.stream()
                .map(DiscussionPost::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = postIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            log.warn("[ES-DB 불일치] ES 인덱스에는 존재하나 DB에 없는 postId: {}", missingIds);
        }

        Map<Long, DiscussionPost> postMap = posts.stream()
                .collect(Collectors.toMap(DiscussionPost::getId, p -> p));

        return postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    // ────────────────────────────────────────────────────────────────
    // 유틸
    // ────────────────────────────────────────────────────────────────

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
