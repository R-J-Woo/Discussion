package com.discussion.ryu.repository;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 성공")
    void saveUser_Success() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser123");
        assertThat(savedUser.getName()).isEqualTo("테스트유저");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("username으로 사용자 조회 성공")
    void findByUsername_Success() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByUsername("testuser123");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser123");
    }

    @Test
    @DisplayName("username으로 사용자 조회 실패 - 존재하지 않는 사용자")
    void findByUsername_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("username 중복 확인 - 중복됨")
    void existsByUsername_Exists() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername("testuser123");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 중복 확인 - 중복 안됨")
    void existsByUsername_NotExists() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복됨")
    void existsByName_Exists() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByName("테스트유저");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복 안됨")
    void existsByName_NotExists() {
        // when
        boolean exists = userRepository.existsByName("존재하지않는닉네임");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복됨")
    void existsByEmail_Exists() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 안됨")
    void existsByEmail_NotExists() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 사용자 제외하고 닉네임 중복 확인 - 중복됨")
    void existsByNameAndUserIdNot_Exists() {
        // given
        User user1 = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser1 = userRepository.save(user1);

        User user2 = createUser("anotheruser", "다른유저", "another@example.com");
        User savedUser2 = userRepository.save(user2);

        // when
        boolean exists = userRepository.existsByNameAndUserIdNot(
                "테스트유저", savedUser2.getUserId()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 사용자 제외하고 닉네임 중복 확인 - 본인 닉네임")
    void existsByNameAndUserIdNot_SameUser() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);

        // when
        boolean exists = userRepository.existsByNameAndUserIdNot(
                "테스트유저", savedUser.getUserId()
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 사용자 제외하고 이메일 중복 확인 - 중복됨")
    void existsByEmailAndUserIdNot_Exists() {
        // given
        User user1 = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser1 = userRepository.save(user1);

        User user2 = createUser("anotheruser", "다른유저", "another@example.com");
        User savedUser2 = userRepository.save(user2);

        // when
        boolean exists = userRepository.existsByEmailAndUserIdNot(
                "test@example.com", savedUser2.getUserId()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자 Soft Delete 동작 확인")
    void softDelete_Success() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getUserId();

        // when
        userRepository.delete(savedUser);
        userRepository.flush();

        // then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty(); // Soft Delete로 인해 조회되지 않음
    }
    
    private User createUser(String username, String name, String email) {
        return User.builder()
                .username(username)
                .password("encodedPassword")
                .name(name)
                .email(email)
                .provider(AuthProvider.LOCAL)
                .providerId(username)
                .build();
    }
}
