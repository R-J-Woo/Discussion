package com.discussion.ryu.controller;

import com.discussion.ryu.dto.user.UpdateUserInfoDto;
import com.discussion.ryu.dto.user.UserLoginDto;
import com.discussion.ryu.dto.user.UserInfoResponse;
import com.discussion.ryu.dto.user.UserSignUpDto;
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
//    @PutMapping("/reissue")
//    public ResponseEntity<UserInfoResponse> updatePassword(@AuthenticationPrincipal User user) {
//        UserInfoResponse userInfoResponse = userService.getMyInfo(user);
//        return ResponseEntity.ok(userInfoResponse);
//    }
//
//    // 사용자 탈퇴
//    @PutMapping("/me")
//    public ResponseEntity<UserInfoResponse> deleteMyInfo(@AuthenticationPrincipal User user) {
//        UserInfoResponse userInfoResponse = userService.getMyInfo(user);
//        return ResponseEntity.ok(userInfoResponse);
//    }
}
