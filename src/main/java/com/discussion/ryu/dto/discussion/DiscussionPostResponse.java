package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.dto.user.UserInfoResponse;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import org.springframework.data.domain.Page;

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
        Page<OpinionResponse> opinions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DiscussionPostResponse from(DiscussionPost post, Page<OpinionResponse> opinions) {
        return new DiscussionPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUserId(),
                post.getAuthor().getName(),
                post.getAgreeCount(),
                post.getDisagreeCount(),
                opinions,
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
                Page.empty(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
