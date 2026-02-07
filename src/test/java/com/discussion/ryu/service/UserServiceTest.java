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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private UserSignUpDto signUpDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        signUpDto = UserSignUpDto.builder()
                .username("testuser")
                .password("password123")
                .name("테스트유저")
                .email("test@example.com")
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
    void signup_Success() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByName(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userService.signup(signUpDto);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username")
    void signup_DuplicateUsername() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpDto))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 name")
    void signup_DuplicateName() {
        // given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByName(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpDto))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessageContaining("이미 존재하는 닉네임입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        UserLoginDto loginDto = UserLoginDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(anyLong(), anyString())).willReturn("jwt-token");

        // when
        String token = userService.login(loginDto);

        // then
        assertThat(token).isEqualTo("jwt-token");
        verify(jwtTokenProvider, times(1)).createToken(testUser.getUserId(), testUser.getUsername());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_UserNotFound() {
        // given
        UserLoginDto loginDto = UserLoginDto.builder()
                .username("nonexistent")
                .password("password123")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_WrongPassword() {
        // given
        UserLoginDto loginDto = UserLoginDto.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        // when
        UserInfoResponse response = userService.getMyInfo(testUser);

        // then
        assertThat(response.username()).isEqualTo(testUser.getUsername());
        assertThat(response.name()).isEqualTo(testUser.getName());
        assertThat(response.email()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyInfo_Success() {
        // given
        UpdateUserInfoDto updateDto = UpdateUserInfoDto.builder()
                .name("수정된이름")
                .email("updated@example.com")
                .build();

        given(userRepository.existsByNameAndUserIdNot(anyString(), anyLong())).willReturn(false);
        given(userRepository.existsByEmailAndUserIdNot(anyString(), anyLong())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        UserInfoResponse response = userService.updateMyInfo(testUser, updateDto);

        // then
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 중복된 닉네임")
    void updateMyInfo_DuplicateName() {
        // given
        UpdateUserInfoDto updateDto = UpdateUserInfoDto.builder()
                .name("중복된이름")
                .email("updated@example.com")
                .build();

        given(userRepository.existsByNameAndUserIdNot(anyString(), anyLong())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateMyInfo(testUser, updateDto))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessageContaining("이미 존재하는 닉네임입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("newPassword123")
                .newPasswordConfirm("newPassword123")
                .build();

        given(passwordEncoder.matches("currentPassword", testUser.getPassword())).willReturn(true);
        given(passwordEncoder.matches("newPassword123", testUser.getPassword())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");

        // when
        userService.updatePassword(testUser, updatePasswordDto);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_InvalidCurrentPassword() {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword123")
                .newPasswordConfirm("newPassword123")
                .build();

        given(passwordEncoder.matches("wrongPassword", testUser.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, updatePasswordDto))
                .isInstanceOf(InvalidCurrentPasswordException.class)
                .hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 불일치")
    void updatePassword_PasswordMismatch() {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("newPassword123")
                .newPasswordConfirm("differentPassword")
                .build();

        given(passwordEncoder.matches("currentPassword", testUser.getPassword())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, updatePasswordDto))
                .isInstanceOf(PasswordConfirmationMismatchException.class)
                .hasMessageContaining("새 비밀번호가 서로 일치하지 않습니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 동일")
    void updatePassword_SameAsOldPassword() {
        // given
        UpdateUserPasswordDto updatePasswordDto = UpdateUserPasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("currentPassword")
                .newPasswordConfirm("currentPassword")
                .build();

        given(passwordEncoder.matches("currentPassword", testUser.getPassword())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(testUser, updatePasswordDto))
                .isInstanceOf(PasswordNotChangedException.class)
                .hasMessageContaining("기존 비밀번호와 다른 비밀번호를 사용해주세요.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 탈퇴 성공")
    void deleteMyInfo_Success() {
        // when
        userService.deleteMyInfo(testUser);

        // then
        verify(userRepository, times(1)).delete(testUser);
    }
}
