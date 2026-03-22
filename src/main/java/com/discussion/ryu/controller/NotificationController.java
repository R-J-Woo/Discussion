package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.entity.Notification;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.NotificationManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림", description = "알림 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManagementService notificationManagementService;

    @Operation(summary = "사용자 알림 목록 조회", description = "현재 사용자의 알림 목록을 페이징으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getUserNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<Notification> notifications = notificationManagementService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications, "알림 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "특정 알림 조회", description = "특정 알림의 상세 정보를 조회합니다.")
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> getNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        Notification notification = notificationManagementService.getNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(notification, "알림 조회 성공", HttpStatus.OK));
    }
}
