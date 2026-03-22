package com.discussion.ryu.dto.fcm;

import com.discussion.ryu.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenDto {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String token;

    @NotBlank(message = "디바이스 타입은 필수입니다.")
    private String deviceType;

    public DeviceType getDeviceType() {
        try {
            return DeviceType.valueOf(this.deviceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 디바이스 타입입니다.");
        }
    }
}
