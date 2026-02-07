package com.discussion.ryu.integration;

import com.discussion.ryu.dto.user.UserLoginDto;
import com.discussion.ryu.dto.user.UserSignUpDto;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User 통합 테스트")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 후 로그인 통합 테스트")
    void signupAndLogin() throws Exception {
        // given - 회원가입
        UserSignUpDto signUpDto = UserSignUpDto.builder()
                .username("testuser123")
                .password("password1234")
                .name("테스트유저")
                .email("test@example.com")
                .build();

        // when - 회원가입 수행
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        // then - 로그인 수행
        UserLoginDto loginDto = UserLoginDto.builder()
                .username("testuser123")
                .password("password1234")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."));
    }

    @Test
    @DisplayName("회원가입 중복 username 검증 통합 테스트")
    void signupWithDuplicateUsername() throws Exception {
        // given - 첫 번째 사용자 등록
        User existingUser = User.builder()
                .username("testuser123")
                .password(passwordEncoder.encode("password1234"))
                .name("기존유저")
                .email("existing@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("testuser123")
                .build();
        userRepository.save(existingUser);

        // when - 동일한 username으로 회원가입 시도
        UserSignUpDto signUpDto = UserSignUpDto.builder()
                .username("testuser123")
                .password("password5678")
                .name("새유저")
                .email("new@example.com")
                .build();

        // then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 통합 테스트")
    void loginWithWrongPassword() throws Exception {
        // given
        User user = User.builder()
                .username("testuser123")
                .password(passwordEncoder.encode("correctpassword"))
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("testuser123")
                .build();
        userRepository.save(user);

        // when
        UserLoginDto loginDto = UserLoginDto.builder()
                .username("testuser123")
                .password("wrongpassword")
                .build();

        // then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
