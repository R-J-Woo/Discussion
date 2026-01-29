package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 중복, 유효성 검증 실패 등)"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody UserSignUpDto userSignUpDto
    ) {
        userService.signup(userSignUpDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "회원가입이 완료되었습니다.", HttpStatus.CREATED));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공, JWT 토큰 반환"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (이메일 또는 비밀번호 오류)"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody UserLoginDto userLoginDto
    ) {
        String token = userService.login(userLoginDto);
        return ResponseEntity.ok(ApiResponse.success(token, "로그인에 성공했습니다.", HttpStatus.OK));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        UserInfoResponse userInfoResponse = userService.getMyInfo(user);
        return ResponseEntity.ok(ApiResponse.success(userInfoResponse, "내 정보 조회에 성공하였습니다.", HttpStatus.OK));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserInfoDto updateUserInfoDto
    ) {
        UserInfoResponse userInfoResponse = userService.updateMyInfo(user, updateUserInfoDto);
        return ResponseEntity.ok(ApiResponse.success(userInfoResponse, "내 정보 수정에 성공하였습니다.", HttpStatus.OK));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (현재 비밀번호 불일치 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserPasswordDto updateUserPasswordDto
    ) {
        userService.updatePassword(user, updateUserPasswordDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "사용자 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        userService.deleteMyInfo(user);
        return ResponseEntity.ok(ApiResponse.success(null, "사용자 탈퇴가 완료되었습니다.", HttpStatus.OK));
    }
}
