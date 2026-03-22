package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
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
    }
}
