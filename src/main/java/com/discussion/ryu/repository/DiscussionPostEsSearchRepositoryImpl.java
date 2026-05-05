package com.discussion.ryu.repository;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.dto.discussion.SortType;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionPostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
public class DiscussionPostEsSearchRepositoryImpl implements DiscussionPostRepositoryCustom {

    private final ElasticsearchOperations esOperations;
    private final DiscussionPostRepository discussionPostRepository;

    @Override
    public Page<DiscussionPost> searchPosts(
            String keyword,
            DiscussionSearchDto.SearchType searchType,
            Pageable pageable,
            SortType sortType) {

        NativeQuery query = buildEsQuery(keyword, searchType, sortType, pageable);
        SearchHits<DiscussionPostDocument> hits =
                esOperations.search(query, DiscussionPostDocument.class);

        List<Long> postIds = hits.stream()
                .map(hit -> hit.getContent().getPostId())
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // JPA로 실제 엔티티 조회 (연관관계 포함)
        List<DiscussionPost> posts = discussionPostRepository.findAllByIdInWithAuthor(postIds);

        // ES 결과 순서 보존
        Map<Long, DiscussionPost> postMap = posts.stream()
                .collect(Collectors.toMap(DiscussionPost::getId, p -> p));
        List<DiscussionPost> ordered = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(ordered, pageable, hits.getTotalHits());
    }

    private NativeQuery buildEsQuery(String keyword,
                                     DiscussionSearchDto.SearchType searchType,
                                     SortType sortType,
                                     Pageable pageable) {
        Query esQuery = hasText(keyword)
                ? buildMatchQuery(keyword, searchType)
                : Query.of(q -> q.matchAll(m -> m));

        SortOptions sort = buildSort(keyword, sortType);

        return NativeQuery.builder()
                .withQuery(esQuery)
                .withSort(sort)
                .withPageable(pageable)
                .build();
    }

    /**
     * 검색 타입에 따라 match / multi_match 쿼리 생성.
     * title·content 필드에 nori 분석기가 적용되어 있어 한국어 형태소 검색이 가능.
     */
    private Query buildMatchQuery(String keyword, DiscussionSearchDto.SearchType searchType) {
        DiscussionSearchDto.SearchType type =
                searchType != null ? searchType : DiscussionSearchDto.SearchType.ALL;

        return switch (type) {
            case TITLE -> Query.of(q -> q
                    .match(m -> m.field("title").query(keyword)));
            case CONTENT -> Query.of(q -> q
                    .match(m -> m.field("content").query(keyword)));
            default -> Query.of(q -> q
                    .multiMatch(m -> m.fields("title", "content").query(keyword)));
        };
    }

    /**
     * 키워드가 있으면 ES 관련도 점수(_score) 기준 정렬.
     * 키워드가 없으면 SortType에 따라 필드 정렬.
     */
    private SortOptions buildSort(String keyword, SortType sortType) {
        if (hasText(keyword)) {
            return SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc)));
        }

        SortType effective = sortType != null ? sortType : SortType.LATEST;
        return switch (effective) {
            case MOST_AGREED -> SortOptions.of(s -> s
                    .field(f -> f.field("agreeCount").order(SortOrder.Desc)));
            case MOST_DISAGREED -> SortOptions.of(s -> s
                    .field(f -> f.field("disagreeCount").order(SortOrder.Desc)));
            case POPULAR -> SortOptions.of(s -> s
                    .field(f -> f.field("agreeCount").order(SortOrder.Desc)));
            default -> SortOptions.of(s -> s
                    .field(f -> f.field("createdAt").order(SortOrder.Desc)));
        };
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
