package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
<<<<<<< HEAD
import com.discussion.ryu.dto.fcm.FcmTokenDto;
import com.discussion.ryu.dto.fcm.FcmTokenResponse;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM", description = "FCM 푸시 알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmTokenService fcmTokenService;

    @Operation(summary = "FCM 토큰 등록/업데이트", description = "사용자의 FCM 토큰을 등록하거나 업데이트합니다.")
    @PostMapping("/tokens")
    public ResponseEntity<ApiResponse<FcmTokenResponse>> registerFcmToken(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody FcmTokenDto fcmTokenDto
    ) {
        FcmTokenResponse response = fcmTokenService.saveFcmToken(user, fcmTokenDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "FCM 토큰이 등록되었습니다.", HttpStatus.CREATED));
=======
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
>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
    }
}
