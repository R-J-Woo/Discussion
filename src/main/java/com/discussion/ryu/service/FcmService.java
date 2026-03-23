package com.discussion.ryu.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
=======
import org.springframework.stereotype.Service;

>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
@Service
public class FcmService {

    public void sendMessage(String token, String title, String body) throws FirebaseMessagingException {
<<<<<<< HEAD
=======

>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
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
<<<<<<< HEAD
        log.info("FCM 메시지 발송 성공: {}", response);
=======
        System.out.println("Successfully sent message: " + response);
>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
    }
}
