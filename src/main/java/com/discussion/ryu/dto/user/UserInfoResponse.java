package com.discussion.ryu.dto.user;

import com.discussion.ryu.entity.User;

import java.time.ZonedDateTime;

public record UserInfoResponse(
    String username,
    String name,
    String email,
    String grade,
    ZonedDateTime created_at,
    ZonedDateTime updated_at,
    ZonedDateTime deleted_at
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getGrade(),
                user.getCreated_at(),
                user.getUpdated_at(),
                user.getDeleted_at()
        );
    }
}
