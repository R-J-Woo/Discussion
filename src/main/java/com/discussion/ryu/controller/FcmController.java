package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.service.FcmService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/api/noti/push")
    public ResponseEntity<ApiResponse<Void>> push(@RequestParam String token) throws FirebaseMessagingException {
        fcmService.sendMessage(token, "알림 제목", "알림 내용");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null, "알림 전송이 완료되었습니다.", HttpStatus.OK));
    }
}
