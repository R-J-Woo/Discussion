package com.discussion.ryu.service;

import com.discussion.ryu.dto.fcm.FcmTokenDto;
import com.discussion.ryu.dto.fcm.FcmTokenResponse;
import com.discussion.ryu.entity.DeviceType;
import com.discussion.ryu.entity.FcmToken;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * FCM 토큰 저장 또는 업데이트
     * 같은 디바이스 타입의 이전 토큰은 삭제 (한 디바이스당 하나의 토큰만 유지)
     */
    @Transactional
    public FcmTokenResponse saveFcmToken(User user, FcmTokenDto fcmTokenDto) {
        String token = fcmTokenDto.getToken();
        DeviceType deviceType = fcmTokenDto.getDeviceType();

        // 같은 디바이스 타입의 이전 토큰이 있으면 삭제
        List<FcmToken> userTokens = fcmTokenRepository.findByUser(user);
        userTokens.stream()
                .filter(t -> t.getDeviceType() == deviceType)
                .forEach(fcmTokenRepository::delete);

        // 새 토큰 저장
        FcmToken newToken = FcmToken.builder()
                .user(user)
                .token(token)
                .deviceType(deviceType)
                .build();

        FcmToken savedToken = fcmTokenRepository.save(newToken);
        return FcmTokenResponse.from(savedToken);
    }

    /**
     * 사용자의 모든 FCM 토큰 조회 (내부용)
     */
    public List<FcmToken> getUserFcmTokens(User user) {
        return fcmTokenRepository.findByUser(user);
    }
}
