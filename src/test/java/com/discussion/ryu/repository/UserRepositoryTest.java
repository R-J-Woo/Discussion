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
    @DisplayName("특정 사용자 제외하고 이메일 중복 확인 - 본인 이메일")
    void existsByEmailAndUserIdNot_SameUser() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmailAndUserIdNot(
                "test@example.com", savedUser.getUserId()
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("중복된 username 저장 시 예외 발생")
    void saveUser_DuplicateUsername_ThrowsException() {
        // given
        User user1 = createUser("testuser123", "테스트유저1", "test1@example.com");
        userRepository.save(user1);

        User user2 = createUser("testuser123", "테스트유저2", "test2@example.com");

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    userRepository.save(user2);
                    userRepository.flush();
                }
        );
    }

    @Test
    @DisplayName("중복된 email 저장 시 예외 발생")
    void saveUser_DuplicateEmail_ThrowsException() {
        // given
        User user1 = createUser("testuser123", "테스트유저1", "test@example.com");
        userRepository.save(user1);

        User user2 = createUser("anotheruser", "테스트유저2", "test@example.com");

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    userRepository.save(user2);
                    userRepository.flush();
                }
        );
    }

    @Test
    @DisplayName("중복된 name 저장 시 예외 발생")
    void saveUser_DuplicateName_ThrowsException() {
        // given
        User user1 = createUser("testuser123", "테스트유저", "test1@example.com");
        userRepository.save(user1);

        User user2 = createUser("anotheruser", "테스트유저", "test2@example.com");

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    userRepository.save(user2);
                    userRepository.flush();
                }
        );
    }

    @Test
    @DisplayName("사용자 정보 수정 시 updatedAt 자동 갱신")
    void updateUserInfo_UpdatedAtChanged() throws InterruptedException {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);
        userRepository.flush();
        
        java.time.LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();
        
        // 시간 차이를 만들기 위한 대기
        Thread.sleep(100);

        // when
        savedUser.updateInfo("수정된유저", "updated@example.com");
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        User updatedUser = userRepository.findById(savedUser.getUserId()).get();
        assertThat(updatedUser.getName()).isEqualTo("수정된유저");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("비밀번호 변경 시 updatedAt 자동 갱신")
    void updatePassword_UpdatedAtChanged() throws InterruptedException {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);
        userRepository.flush();
        
        java.time.LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();
        
        // 시간 차이를 만들기 위한 대기
        Thread.sleep(100);

        // when
        savedUser.updatePassword("newEncodedPassword");
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        User updatedUser = userRepository.findById(savedUser.getUserId()).get();
        assertThat(updatedUser.getPassword()).isEqualTo("newEncodedPassword");
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
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

    @Test
    @DisplayName("Soft Delete 후 findByUsername으로 조회되지 않음")
    void softDelete_FindByUsername_NotFound() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);

        // when
        userRepository.delete(savedUser);
        userRepository.flush();

        // then
        Optional<User> deletedUser = userRepository.findByUsername("testuser123");
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Soft Delete 후 existsByUsername으로 확인 시 false 반환")
    void softDelete_ExistsByUsername_ReturnsFalse() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        userRepository.delete(user);
        userRepository.flush();

        // then
        boolean exists = userRepository.existsByUsername("testuser123");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Soft Delete 후 existsByEmail으로 확인 시 false 반환")
    void softDelete_ExistsByEmail_ReturnsFalse() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        userRepository.delete(user);
        userRepository.flush();

        // then
        boolean exists = userRepository.existsByEmail("test@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Soft Delete 후 existsByName으로 확인 시 false 반환")
    void softDelete_ExistsByName_ReturnsFalse() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        userRepository.save(user);

        // when
        userRepository.delete(user);
        userRepository.flush();

        // then
        boolean exists = userRepository.existsByName("테스트유저");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Soft Delete 후 동일한 username으로 재가입 가능")
    void softDelete_CanReuseUsername() {
        // given
        User user1 = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser1 = userRepository.save(user1);
        
        // when - 첫 번째 사용자 삭제
        userRepository.delete(savedUser1);
        userRepository.flush();
        
        // then - 동일한 username으로 새 사용자 생성 가능
        User user2 = createUser("testuser123", "새로운유저", "new@example.com");
        User savedUser2 = userRepository.save(user2);
        
        assertThat(savedUser2.getUserId()).isNotNull();
        assertThat(savedUser2.getUsername()).isEqualTo("testuser123");
        assertThat(savedUser2.getName()).isEqualTo("새로운유저");
    }

    @Test
    @DisplayName("updateInfo 메서드로 이름과 이메일 변경")
    void updateInfo_Success() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);

        // when
        savedUser.updateInfo("변경된이름", "changed@example.com");
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        User updatedUser = userRepository.findById(savedUser.getUserId()).get();
        assertThat(updatedUser.getName()).isEqualTo("변경된이름");
        assertThat(updatedUser.getEmail()).isEqualTo("changed@example.com");
        assertThat(updatedUser.getUsername()).isEqualTo("testuser123"); // username은 변경되지 않음
    }

    @Test
    @DisplayName("updatePassword 메서드로 비밀번호 변경")
    void updatePassword_Success() {
        // given
        User user = createUser("testuser123", "테스트유저", "test@example.com");
        User savedUser = userRepository.save(user);
        String originalPassword = savedUser.getPassword();

        // when
        savedUser.updatePassword("newPassword123");
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        User updatedUser = userRepository.findById(savedUser.getUserId()).get();
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword123");
        assertThat(updatedUser.getPassword()).isNotEqualTo(originalPassword);
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
