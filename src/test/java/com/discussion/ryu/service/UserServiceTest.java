package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.user.*;
import com.discussion.ryu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
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
    void signup_Success() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByName(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userService.signup(signUpDto);

        // then
        verify(userRepository, times(1)).existsByUsername("testuser123");
        verify(userRepository, times(1)).existsByName("테스트유저");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 아이디")
    void signup_Fail_DuplicateUsername() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpDto))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("이미 존재하는 아이디입니다.");

        verify(userRepository, times(1)).existsByUsername("testuser123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 닉네임")
    void signup_Fail_DuplicateName() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByName(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpDto))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");

        verify(userRepository, times(1)).existsByName("테스트유저");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(anyLong(), anyString())).willReturn("jwt-token");

        // when
        String token = userService.login(loginDto);

        // then
        assertThat(token).isEqualTo("jwt-token");
        verify(userRepository, times(1)).findByUsername("testuser123");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, times(1)).createToken(1L, "testuser123");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {
        // given
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("아이디 또는 비밀번호가 틀렸습니다.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // given
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("아이디 또는 비밀번호가 틀렸습니다.");

        verify(jwtTokenProvider, never()).createToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        // when
        UserInfoResponse response = userService.getMyInfo(testUser);

        // then
        assertThat(response.username()).isEqualTo("testuser123");
        assertThat(response.name()).isEqualTo("테스트유저");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyInfo_Success() {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("수정된이름", "updated@example.com");

        given(userRepository.existsByNameAndUserIdNot(anyString(), anyLong())).willReturn(false);
        given(userRepository.existsByEmailAndUserIdNot(anyString(), anyLong())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        UserInfoResponse response = userService.updateMyInfo(testUser, updateDto);

        // then
        verify(userRepository, times(1)).existsByNameAndUserIdNot("수정된이름", 1L);
        verify(userRepository, times(1)).existsByEmailAndUserIdNot("updated@example.com", 1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 닉네임")
    void updateMyInfo_Fail_DuplicateName() {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("중복된이름", "updated@example.com");

        given(userRepository.existsByNameAndUserIdNot(anyString(), anyLong())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateMyInfo(testUser, updateDto))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 이메일")
    void updateMyInfo_Fail_DuplicateEmail() {
        // given
        UpdateUserInfoDto updateDto = createUpdateUserInfoDto("수정된이름", "duplicate@example.com");

        given(userRepository.existsByNameAndUserIdNot(anyString(), anyLong())).willReturn(false);
        given(userRepository.existsByEmailAndUserIdNot(anyString(), anyLong())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateMyInfo(testUser, updateDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "password123", "newPassword123", "newPassword123"
        );

        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(passwordEncoder.matches("newPassword123", "encodedPassword")).willReturn(false);
        given(passwordEncoder.encode("newPassword123")).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userService.updatePassword(testUser, passwordDto);

        // then
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_Fail_InvalidCurrentPassword() {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "wrongPassword", "newPassword123", "newPassword123"
        );

        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, passwordDto))
                .isInstanceOf(InvalidCurrentPasswordException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 확인 불일치")
    void updatePassword_Fail_PasswordConfirmationMismatch() {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "password123", "newPassword123", "differentPassword123"
        );

        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, passwordDto))
                .isInstanceOf(PasswordConfirmationMismatchException.class)
                .hasMessage("새 비밀번호가 서로 일치하지 않습니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 동일")
    void updatePassword_Fail_SameAsOldPassword() {
        // given
        UpdateUserPasswordDto passwordDto = createUpdatePasswordDto(
                "password123", "password123", "password123"
        );

        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, passwordDto))
                .isInstanceOf(PasswordNotChangedException.class)
                .hasMessage("기존 비밀번호와 다른 비밀번호를 사용해주세요.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMyInfo_Success() {
        // when
        userService.deleteMyInfo(testUser);

        // then
        verify(userRepository, times(1)).delete(testUser);
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
