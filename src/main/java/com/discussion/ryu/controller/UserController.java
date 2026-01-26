package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        userService.signup(userSignUpDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "회원가입이 완료되었습니다.", HttpStatus.CREATED));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        String token = userService.login(userLoginDto);
        return ResponseEntity.ok(ApiResponse.success(token, "로그인에 성공했습니다.", HttpStatus.OK));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(@AuthenticationPrincipal User user) {
        UserInfoResponse userInfoResponse = userService.getMyInfo(user);
        return ResponseEntity.ok(ApiResponse.success(userInfoResponse, "내 정보 조회에 성공하였습니다.", HttpStatus.OK));
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserInfoDto updateUserInfoDto
    ) {
        UserInfoResponse userInfoResponse = userService.updateMyInfo(user, updateUserInfoDto);
        return ResponseEntity.ok(ApiResponse.success(userInfoResponse, "내 정보 수정에 성공하였습니다.", HttpStatus.OK));
    }

    // 비밀번호 변경
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserPasswordDto updateUserPasswordDto
    ) {
        userService.updatePassword(user, updateUserPasswordDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다.", HttpStatus.OK));
    }

    // 사용자 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyInfo(@AuthenticationPrincipal User user) {
        userService.deleteMyInfo(user);
        return ResponseEntity.ok(ApiResponse.success(null, "사용자 탈퇴가 완료되었습니다.", HttpStatus.OK));
    }
}
