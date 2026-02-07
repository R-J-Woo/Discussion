package com.discussion.ryu.controller;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.user.*;
import com.discussion.ryu.service.CustomUserDetailsService;
import com.discussion.ryu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserSignUpDto signUpDto;
    private UserLoginDto loginDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        signUpDto = UserSignUpDto.builder()
                .username("testuser")
                .password("password123")
                .name("테스트유저")
                .email("test@example.com")
                .build();

        loginDto = UserLoginDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .password("encodedPassword")
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("testuser")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() throws Exception {
        // given
        doNothing().when(userService).signup(any(UserSignUpDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        verify(userService, times(1)).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username")
    void signUp_DuplicateUsername() throws Exception {
        // given
        doThrow(new DuplicateUsernameException("이미 존재하는 아이디입니다."))
                .when(userService).signup(any(UserSignUpDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (빈 username)")
    void signUp_ValidationFail() throws Exception {
        // given
        UserSignUpDto invalidDto = UserSignUpDto.builder()
                .username("")
                .password("password123")
                .name("테스트유저")
                .email("test@example.com")
                .build();

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        String token = "jwt-token-123";
        given(userService.login(any(UserLoginDto.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(token))
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."));

        verify(userService, times(1)).login(any(UserLoginDto.class));
    }

    @Test
    @DisplayName("로그인 실패 - 인증 실패")
    void login_AuthenticationFailed() throws Exception {
        // given
        given(userService.login(any(UserLoginDto.class)))
                .willThrow(new AuthenticationFailedException("아이디 또는 비밀번호가 틀렸습니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser
    void getMyInfo_Success() throws Exception {
        // given
        UserInfoResponse response = UserInfoResponse.builder()
                .username("testuser")
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .build();

        given(userService.getMyInfo(any(User.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("테스트유저"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    @WithMockUser
    void updateMyInfo_Success() throws Exception {
        // given
        UpdateUserInfoDto updateDto = UpdateUserInfoDto.builder()
                .name("수정된이름")
                .email("updated@example.com")
                .build();

        UserInfoResponse response = UserInfoResponse.builder()
                .username("testuser")
                .name("수정된이름")
                .email("updated@example.com")
                .provider(AuthProvider.LOCAL)
                .build();

        given(userService.updateMyInfo(any(User.class), any(UpdateUserInfoDto.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된이름"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 닉네임")
    @WithMockUser
    void updateMyInfo_DuplicateName() throws Exception {
        // given
        UpdateUserInfoDto updateDto = UpdateUserInfoDto.builder()
                .name("중복된이름")
                .email("updated@example.com")
                .build();

        given(userService.updateMyInfo(any(User.class), any(UpdateUserInfoDto.class)))
                .willThrow(new DuplicateNameException("이미 존재하는 닉네임입니다."));

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    @WithMockUser
    void updatePassword_Success() throws Exception {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("newPassword123")
                .newPasswordConfirm("newPassword123")
                .build();

        doNothing().when(userService).updatePassword(any(User.class), any(UpdateUserPasswordDto.class));

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    @WithMockUser
    void updatePassword_InvalidCurrentPassword() throws Exception {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword123")
                .newPasswordConfirm("newPassword123")
                .build();

        doThrow(new InvalidCurrentPasswordException("현재 비밀번호가 일치하지 않습니다."))
                .when(userService).updatePassword(any(User.class), any(UpdateUserPasswordDto.class));

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 탈퇴 성공")
    @WithMockUser
    void deleteMyInfo_Success() throws Exception {
        // given
        doNothing().when(userService).deleteMyInfo(any(User.class));

        // when & then
        mockMvc.perform(delete("/api/users/me")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 탈퇴가 완료되었습니다."));

        verify(userService, times(1)).deleteMyInfo(any(User.class));
    }
}
