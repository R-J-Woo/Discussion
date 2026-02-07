package com.discussion.ryu.dto.user;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserInfoResponse(
    String username,
    String name,
    String email,
    AuthProvider provider,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
