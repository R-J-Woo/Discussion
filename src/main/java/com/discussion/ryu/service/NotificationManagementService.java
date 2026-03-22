package com.discussion.ryu.service;

import com.discussion.ryu.entity.Notification;
import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationManagementService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final FcmService fcmService;

    /**
     * 새 의견 등록 알림 발송 및 저장
     */
    @Transactional
    public void notifyNewOpinion(User postAuthor, Opinion opinion, String opinionAuthorName) {
        // 토론글 작성자가 의견 작성자와 같으면 알림 안 보냄
        if (postAuthor.getUserId().equals(opinion.getAuthor().getUserId())) {
            return;
        }

        String title = "새로운 의견이 등록되었습니다";
        String body = opinionAuthorName + "님이 당신의 토론글에 의견을 남겼습니다";

        // Notification 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .user(postAuthor)
                .opinion(opinion)
                .title(title)
                .body(body)
                .isSent(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // FCM 토큰으로 실제 푸시 알림 발송
        sendFcmNotification(postAuthor, title, body, savedNotification);
    }

    /**
     * FCM 푸시 알림 발송 (내부 메서드)
     * notifyNewOpinion()의 트랜잭션 범위 내에서 실행됨
     */
    private void sendFcmNotification(User recipient, String title, String body,
                                      Notification notification) {
        List<com.discussion.ryu.entity.FcmToken> tokens = fcmTokenService.getUserFcmTokens(recipient);

        if (tokens.isEmpty()) {
            log.info("사용자 {}의 FCM 토큰이 없어서 푸시 알림을 발송하지 않습니다.", recipient.getUserId());
            notification.setSent(false);  // 발송 못함 표시
            notificationRepository.save(notification);  // 이력은 저장
            return;
        }

        for (com.discussion.ryu.entity.FcmToken fcmToken : tokens) {
            try {
                fcmService.sendMessage(fcmToken.getToken(), title, body);
                log.info("FCM 알림 발송 성공 - 사용자: {}, 의견: {}", recipient.getUserId(), notification.getOpinion().getId());
                notification.setSent(true);
            } catch (Exception e) {
                log.error("FCM 알림 발송 실패 - 사용자: {}, 이유: {}", recipient.getUserId(), e.getMessage());
                // 발송 실패해도 알림 이력은 저장됨
            }
        }

        notificationRepository.save(notification);
    }

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUser(user, pageable);
    }

    /**
     * 특정 알림 조회
     */
    public Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
    }
}
