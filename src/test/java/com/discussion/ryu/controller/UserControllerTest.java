package com.discussion.ryu.controller;

import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.user.*;
import com.discussion.ryu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserSignUpDto signUpDto;
    private UserLoginDto loginDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        signUpDto = createSignUpDto("testuser123", "password123", "테스트유저", "test@example.com");
        loginDto = createLoginDto("testuser123", "password123");
        testUser = createUser(1L, "testuser123", "encodedPassword", "테스트유저", "test@example.com");
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService, times(1)).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (짧은 아이디)")
    void signUp_Fail_ValidationError_ShortUsername() throws Exception {
        // given
        UserSignUpDto invalidDto = createSignUpDto("short", "password123", "테스트유저", "test@example.com");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (잘못된 이메일)")
    void signUp_Fail_ValidationError_InvalidEmail() throws Exception {
        // given
        UserSignUpDto invalidDto = createSignUpDto("testuser123", "password123", "테스트유저", "invalid-email");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 아이디")
    void signUp_Fail_DuplicateUsername() throws Exception {
        // given
        doThrow(new DuplicateUsernameException("이미 존재하는 아이디입니다."))
                .when(userService).signup(any(UserSignUpDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void signUp_Fail_DuplicateEmail() throws Exception {
        // given
        doThrow(new DuplicateEmailException("이미 존재하는 이메일입니다."))
                .when(userService).signup(any(UserSignUpDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 닉네임")
    void signUp_Fail_DuplicateName() throws Exception {
        // given
        doThrow(new DuplicateNameException("이미 존재하는 닉네임입니다."))
                .when(userService).signup(any(UserSignUpDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (짧은 비밀번호)")
    void signUp_Fail_ValidationError_ShortPassword() throws Exception {
        // given
        UserSignUpDto invalidDto = createSignUpDto("testuser123", "short", "테스트유저", "test@example.com");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (null 값)")
    void signUp_Fail_ValidationError_NullValues() throws Exception {
        // given
        UserSignUpDto invalidDto = createSignUpDto(null, null, null, null);

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).signup(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        String expectedToken = "jwt-token-12345";
        given(userService.login(any(UserLoginDto.class))).willReturn(expectedToken);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(expectedToken))
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."));

        verify(userService, times(1)).login(any(UserLoginDto.class));
    }

    @Test
    @DisplayName("로그인 실패 - 인증 실패")
    void login_Fail_AuthenticationFailed() throws Exception {
        // given
        given(userService.login(any(UserLoginDto.class)))
                .willThrow(new AuthenticationFailedException("아이디 또는 비밀번호가 틀렸습니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 유효성 검증 실패")
    void login_Fail_ValidationError() throws Exception {
        // given
        UserLoginDto invalidDto = createLoginDto(null, null);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).login(any(UserLoginDto.class));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given
        UserInfoResponse response = UserInfoResponse.builder()
                .username("testuser123")
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .build();

        given(userService.getMyInfo(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(user(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser123"))
                .andExpect(jsonPath("$.data.name").value("테스트유저"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.message").value("내 정보 조회에 성공하였습니다."));
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyInfo_Success() throws Exception {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("수정된이름", "updated@example.com");

        UserInfoResponse response = UserInfoResponse.builder()
                .username("testuser123")
                .name("수정된이름")
                .email("updated@example.com")
                .provider(AuthProvider.LOCAL)
                .build();

        given(userService.updateMyInfo(any(), any(UpdateUserInfoDto.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser123"))
                .andExpect(jsonPath("$.data.name").value("수정된이름"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.message").value("내 정보 수정에 성공하였습니다."));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 닉네임")
    void updateMyInfo_Fail_DuplicateName() throws Exception {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("중복된이름", "updated@example.com");

        given(userService.updateMyInfo(any(), any(UpdateUserInfoDto.class)))
                .willThrow(new DuplicateNameException("이미 존재하는 닉네임입니다."));

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 이메일")
    void updateMyInfo_Fail_DuplicateEmail() throws Exception {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("새이름", "duplicate@example.com");

        given(userService.updateMyInfo(any(), any(UpdateUserInfoDto.class)))
                .willThrow(new DuplicateEmailException("이미 존재하는 이메일입니다."));

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 유효성 검증 실패")
    void updateMyInfo_Fail_ValidationError() throws Exception {
        // given
        UpdateUserInfoDto invalidDto = createUpdateUserInfoDto(null, "invalid-email");

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateMyInfo(any(), any(UpdateUserInfoDto.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() throws Exception {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "password123", "newPassword123", "newPassword123"
        );

        doNothing().when(userService).updatePassword(any(), any(UpdateUserPasswordDto.class));

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_Fail_InvalidCurrentPassword() throws Exception {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "wrongPassword", "newPassword123", "newPassword123"
        );

        doThrow(new InvalidCurrentPasswordException("현재 비밀번호가 일치하지 않습니다."))
                .when(userService).updatePassword(any(), any(UpdateUserPasswordDto.class));

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 불일치")
    void updatePassword_Fail_PasswordConfirmationMismatch() throws Exception {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "password123", "newPassword123", "differentPassword123"
        );

        doThrow(new PasswordConfirmationMismatchException("새 비밀번호가 일치하지 않습니다."))
                .when(userService).updatePassword(any(), any(UpdateUserPasswordDto.class));

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("새 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유효성 검증 실패")
    void updatePassword_Fail_ValidationError() throws Exception {
        // given
        UpdateUserPasswordDto invalidDto = createUpdatePasswordDto(null, "short", "short");

        // when & then
        mockMvc.perform(put("/api/users/me/password")
                        .with(csrf())
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updatePassword(any(), any(UpdateUserPasswordDto.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMyInfo_Success() throws Exception {
        // given
        doNothing().when(userService).deleteMyInfo(any(User.class));

        // when & then
        mockMvc.perform(delete("/api/users/me")
                        .with(csrf())
                        .with(user(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 탈퇴가 완료되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService, times(1)).deleteMyInfo(any());
    }

    /**
     * 테스트용 User 객체 생성 헬퍼 메소드
     */
    private User createUser(Long userId, String username, String password, String name, String email) {
        return User.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .provider(AuthProvider.LOCAL)
                .providerId(username)
                .build();
    }

    /**
     * 테스트용 UserSignUpDto 생성 헬퍼 메소드
     */
    private UserSignUpDto createSignUpDto(String username, String password, String name, String email) {
        return UserSignUpDto.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .build();
    }

    /**
     * 테스트용 UserLoginDto 생성 헬퍼 메소드
     */
    private UserLoginDto createLoginDto(String username, String password) {
        return UserLoginDto.builder()
                .username(username)
                .password(password)
                .build();
    }

    /**
     * 테스트용 UpdateUserInfoDto 생성 헬퍼 메소드
     */
    private UpdateUserInfoDto createUpdateUserInfoDto(String name, String email) {
        return UpdateUserInfoDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    /**
     * 테스트용 UpdateUserPasswordDto 생성 헬퍼 메소드
     */
    private UpdateUserPasswordDto createUpdatePasswordDto(String currentPassword, String newPassword, String newPasswordConfirm) {
        return UpdateUserPasswordDto.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .newPasswordConfirm(newPasswordConfirm)
                .build();
    }
}
