package com.discussion.ryu.repository;

import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.QDiscussionPost;
import com.discussion.ryu.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
            String authorName,
            Pageable pageable
    ) {
        // 1. 동적 WHERE 절 생성
        BooleanBuilder whereClause = buildWhereClause(keyword, searchType, authorName);

        // 2. 전체 개수 조회
        long total = queryFactory
                .selectFrom(discussionPost)
                .join(discussionPost.author, user)
                .where(whereClause)
                .distinct()
                .fetchCount();

        // 3. 페이징된 결과 조회
        List<DiscussionPost> results = queryFactory
                .selectFrom(discussionPost)
                .join(discussionPost.author, user).fetchJoin()  // N+1 방지
                .where(whereClause)
                .distinct()
                .orderBy(discussionPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanBuilder buildWhereClause(String keyword, DiscussionSearchDto.SearchType searchType, String authorName) {

        BooleanBuilder where = new BooleanBuilder();

        // 삭제되지 않은 항목
        where.and(discussionPost.deletedAt.isNull());

        // 키워드 있으면 추가
        if (hasText(keyword)) {
            where.and(buildKeywordExpression(keyword, searchType));
        }

        // 작성자명 있으면 추가
        if (hasText(authorName)) {
            where.and(user.name.like("%" + authorName + "%"));
        }

        return where;
    }

    private BooleanExpression buildKeywordExpression(String keyword, DiscussionSearchDto.SearchType searchType) {

        String likeKeyword = "%" + keyword + "%";

        return switch (searchType != null ? searchType : DiscussionSearchDto.SearchType.ALL) {
            case TITLE -> discussionPost.title.like(likeKeyword);
            case CONTENT -> discussionPost.content.like(likeKeyword);
            case ALL -> discussionPost.title.like(likeKeyword)
                    .or(discussionPost.content.like(likeKeyword));
            default -> discussionPost.title.like(likeKeyword)
                    .or(discussionPost.content.like(likeKeyword));
        };
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
