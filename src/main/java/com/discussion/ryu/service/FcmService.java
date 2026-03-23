package com.discussion.ryu.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendMessage(String token, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        log.info("FCM 메시지 발송 성공: {}", response);
    }
}
