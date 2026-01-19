package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.dto.user.UserInfoResponse;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;

import java.time.LocalDateTime;

public record DiscussionPostResponse(
        String title,
        String content,
        User author,
        Long agreeCount,
        Long disagreeCount,
        LocalDateTime createAt,
        LocalDateTime updatedAt
) {
    public static DiscussionPostResponse from(DiscussionPost post) {
        return new DiscussionPostResponse(
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getAgreeCount(),
                post.getDisagreeCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
