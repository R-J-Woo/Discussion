package com.discussion.ryu.dto.user;

import com.discussion.ryu.entity.User;

import java.time.ZonedDateTime;

public record UserInfoResponse(
    String username,
    String name,
    String email,
    String grade,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getGrade(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
