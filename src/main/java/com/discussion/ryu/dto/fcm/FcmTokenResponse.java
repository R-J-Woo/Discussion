package com.discussion.ryu.dto.fcm;

import com.discussion.ryu.entity.DeviceType;
import com.discussion.ryu.entity.FcmToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenResponse {

    private Long id;

    private String token;

    private DeviceType deviceType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static FcmTokenResponse from(FcmToken fcmToken) {
        return FcmTokenResponse.builder()
                .id(fcmToken.getId())
                .token(fcmToken.getToken())
                .deviceType(fcmToken.getDeviceType())
                .createdAt(fcmToken.getCreatedAt())
                .updatedAt(fcmToken.getUpdatedAt())
                .build();
    }
}
