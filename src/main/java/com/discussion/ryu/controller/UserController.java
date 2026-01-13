package com.discussion.ryu.controller;

import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        userService.signup(userSignUpDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        String token = userService.login(userLoginDto);
        return ResponseEntity.ok(token);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(@AuthenticationPrincipal User user) {
        UserInfoResponse userInfoResponse = userService.getMyInfo(user);
        return ResponseEntity.ok(userInfoResponse);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateMyInfo(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserInfoDto updateUserInfoDto
    ) {
        UserInfoResponse userInfoResponse = userService.updateMyInfo(user, updateUserInfoDto);
        return ResponseEntity.ok(userInfoResponse);
    }

    // 비밀번호 변경
    @PutMapping("/me/password")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserPasswordDto updateUserPasswordDto
    ) {
        userService.updatePassword(user, updateUserPasswordDto);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 사용자 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyInfo(@AuthenticationPrincipal User user) {
        userService.deleteMyInfo(user);
        return ResponseEntity.ok("사용자 탈퇴가 완료되었습니다.");
    }
}
