package com.discussion.ryu.service;

import com.discussion.ryu.entity.FcmToken;
import com.discussion.ryu.entity.Notification;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final FcmService fcmService;

    /**
     * FCM 푸시 알림 발송 (내부 메서드)
     * notifyNewOpinion()의 트랜잭션 범위 내에서 실행됨
     */
    @Async("notificationAsyncExecutor")
    @Transactional
    public void sendFcmNotification(User recipient, String title, String body, Notification notification) {

        List<FcmToken> tokens = fcmTokenService.getUserFcmTokens(recipient);

        if (tokens.isEmpty()) {
            notification.setSent(false);  // 발송 못함 표시
            notificationRepository.save(notification);  // 이력은 저장
            return;
        }

        for (FcmToken token : tokens) {
            try {
                fcmService.sendMessage(token.getToken(), title, body);
                log.info("FCM 알림 발송 성공 - 사용자: {}, 의견: {}", recipient.getUserId(), notification.getOpinion().getId());
                notification.setSent(true);
            } catch (Exception e) {
                log.error("FCM 알림 발송 실패 - 사용자: {}, 이유: {}", recipient.getUserId(), e.getMessage());
                // 발송 실패해도 알림 이력은 저장됨
            }
        }

        notificationRepository.save(notification);
    }
}
