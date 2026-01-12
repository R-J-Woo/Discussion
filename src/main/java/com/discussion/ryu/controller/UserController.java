package com.discussion.ryu.controller;

import com.discussion.ryu.dto.user.UserLoginDto;
import com.discussion.ryu.dto.user.UserMeResponse;
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
    public ResponseEntity<UserMeResponse> getMyInfo(@AuthenticationPrincipal User user) {
        UserMeResponse userMeResponse = userService.getMyInfo(user);
        return ResponseEntity.ok(userMeResponse);
    }
}
