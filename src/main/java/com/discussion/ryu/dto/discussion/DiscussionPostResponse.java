package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.dto.user.UserInfoResponse;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record DiscussionPostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorName,
        Long agreeCount,
        Long disagreeCount,
        List<OpinionResponse> opinions,
        Long opinionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DiscussionPostResponse from(DiscussionPost post, List<OpinionResponse> opinions) {
        return new DiscussionPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUserId(),
                post.getAuthor().getName(),
                post.getAgreeCount(),
                post.getDisagreeCount(),
                opinions,
                (long) (opinions.size()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static DiscussionPostResponse from(DiscussionPost post) {
        return new DiscussionPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUserId(),
                post.getAuthor().getName(),
                post.getAgreeCount(),
                post.getDisagreeCount(),
                List.of(),
                0L,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
