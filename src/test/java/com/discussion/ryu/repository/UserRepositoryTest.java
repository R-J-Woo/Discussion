package com.discussion.ryu.repository;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("testuser")
                .build();
    }

    @Test
    @DisplayName("사용자 저장 테스트")
    void saveUser() {
        // when
        User savedUser = userRepository.save(testUser);

        // then
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getName()).isEqualTo("테스트유저");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("username으로 사용자 찾기 성공")
    void findByUsername_Success() {
        // given
        userRepository.save(testUser);

        // when
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("username으로 사용자 찾기 실패")
    void findByUsername_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("username 존재 여부 확인 - 존재함")
    void existsByUsername_True() {
        // given
        userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByUsername("testuser");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 존재 여부 확인 - 존재하지 않음")
    void existsByUsername_False() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("name 존재 여부 확인 - 존재함")
    void existsByName_True() {
        // given
        userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByName("테스트유저");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("name 존재 여부 확인 - 존재하지 않음")
    void existsByName_False() {
        // when
        boolean exists = userRepository.existsByName("존재하지않는유저");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 userId를 제외한 name 존재 여부 확인 - 존재함")
    void existsByNameAndUserIdNot_True() {
        // given
        User savedUser = userRepository.save(testUser);
        User anotherUser = User.builder()
                .username("anotheruser")
                .password("password")
                .name("다른테스트유저")
                .email("another@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("anotheruser")
                .build();
        userRepository.save(anotherUser);

        // when
        boolean exists = userRepository.existsByNameAndUserIdNot("다른테스트유저", savedUser.getUserId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 userId를 제외한 name 존재 여부 확인 - 존재하지 않음")
    void existsByNameAndUserIdNot_False() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByNameAndUserIdNot("테스트유저", savedUser.getUserId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("provider와 providerId로 사용자 찾기 성공")
    void findByProviderAndProviderId_Success() {
        // given
        userRepository.save(testUser);

        // when
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(
                AuthProvider.LOCAL, "testuser");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(foundUser.get().getProviderId()).isEqualTo("testuser");
    }
}
