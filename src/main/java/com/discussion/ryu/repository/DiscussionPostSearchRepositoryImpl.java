package com.discussion.ryu.repository;

import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.dto.discussion.SortType;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.QDiscussionPost;
import com.discussion.ryu.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiscussionPostSearchRepositoryImpl implements DiscussionPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QDiscussionPost discussionPost = QDiscussionPost.discussionPost;
    private static final QUser user = QUser.user;

    @Override
    public Page<DiscussionPost> searchPosts(
            String keyword,
            DiscussionSearchDto.SearchType searchType,
            Pageable pageable,
            SortType sortType
    ) {
        BooleanBuilder whereClause = buildWhereClause(keyword, searchType);

        long total = queryFactory
                .selectFrom(discussionPost)
                .join(discussionPost.author, user)
                .where(whereClause)
                .distinct()
                .fetchCount();

        List<DiscussionPost> results = queryFactory
                .selectFrom(discussionPost)
                .join(discussionPost.author, user).fetchJoin()
                .where(whereClause)
                .distinct()
                .orderBy(buildOrderSpecifiers(keyword, searchType, sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanBuilder buildWhereClause(String keyword, DiscussionSearchDto.SearchType searchType) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(discussionPost.deletedAt.isNull());

        if (hasText(keyword)) {
            where.and(buildFullTextExpression(keyword, searchType).gt(0));
        }

        return where;
    }

    /**
     * keyword 유무 및 SortType에 따라 ORDER BY 절 구성.
     * keyword가 있으면 관련도 점수를 1순위로 추가하고, 그 다음 SortType 정렬.
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(String keyword, DiscussionSearchDto.SearchType searchType, SortType sortType) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // keyword가 있을 때만 관련도 점수 추가 (없으면 아예 추가하지 않음)
        if (hasText(keyword)) {
            orders.add(buildFullTextExpression(keyword, searchType).desc());
        }

        SortType effectiveSortType = sortType != null ? sortType : SortType.LATEST;
        switch (effectiveSortType) {
            case POPULAR -> {
                orders.add(discussionPost.agreeCount.add(discussionPost.disagreeCount).desc());
                orders.add(discussionPost.createdAt.desc());
            }
            case MOST_AGREED -> {
                orders.add(discussionPost.agreeCount.desc());
                orders.add(discussionPost.createdAt.desc());
            }
            case MOST_DISAGREED -> {
                orders.add(discussionPost.disagreeCount.desc());
                orders.add(discussionPost.createdAt.desc());
            }
            default -> orders.add(discussionPost.createdAt.desc()); // LATEST
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    /**
     * 검색 타입에 따라 FULLTEXT 관련도 점수를 반환하는 NumberTemplate 생성.
     * CustomFunctionsContributor에 등록된 match_against 함수를 사용하여
     * MySQL MATCH ... AGAINST (... IN BOOLEAN MODE) SQL로 변환됨.
     */
    private NumberTemplate<Double> buildFullTextExpression(String keyword, DiscussionSearchDto.SearchType searchType) {

        String processed = "+" + keyword + "*";

        return switch (searchType != null ? searchType : DiscussionSearchDto.SearchType.ALL) {
            case TITLE -> Expressions.numberTemplate(Double.class,
                    "function('match_against_single', {0}, {1})",
                    discussionPost.title, processed);
            case CONTENT -> Expressions.numberTemplate(Double.class,
                    "function('match_against_single', {0}, {1})",
                    discussionPost.content, processed);
            default -> Expressions.numberTemplate(Double.class,
                    "function('match_against_multi', {0}, {1}, {2})",
                    discussionPost.title, discussionPost.content, processed);
        };
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
