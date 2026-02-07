package com.discussion.ryu.repository;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("DiscussionPostRepository 테스트")
class DiscussionPostRepositoryTest {

    @Autowired
    private DiscussionPostRepository discussionPostRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private DiscussionPost testPost;

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
        testUser = userRepository.save(testUser);

        testPost = DiscussionPost.builder()
                .title("테스트 토론 주제")
                .content("테스트 토론 내용입니다.")
                .author(testUser)
                .agreeCount(0L)
                .disagreeCount(0L)
                .build();
    }

    @Test
    @DisplayName("토론글 저장 테스트")
    void saveDiscussionPost() {
        // when
        DiscussionPost savedPost = discussionPostRepository.save(testPost);

        // then
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("테스트 토론 주제");
        assertThat(savedPost.getContent()).isEqualTo("테스트 토론 내용입니다.");
        assertThat(savedPost.getAuthor()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("토론글 ID로 조회 성공")
    void findById_Success() {
        // given
        DiscussionPost savedPost = discussionPostRepository.save(testPost);

        // when
        Optional<DiscussionPost> foundPost = discussionPostRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("테스트 토론 주제");
    }

    @Test
    @DisplayName("토론글 ID로 조회 실패")
    void findById_NotFound() {
        // when
        Optional<DiscussionPost> foundPost = discussionPostRepository.findById(999L);

        // then
        assertThat(foundPost).isEmpty();
    }

    @Test
    @DisplayName("전체 토론글 페이징 조회")
    void findAll_Paging() {
        // given
        for (int i = 0; i < 25; i++) {
            DiscussionPost post = DiscussionPost.builder()
                    .title("토론 주제 " + i)
                    .content("토론 내용 " + i)
                    .author(testUser)
                    .agreeCount(0L)
                    .disagreeCount(0L)
                    .build();
            discussionPostRepository.save(post);
        }

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        // when
        Page<DiscussionPost> posts = discussionPostRepository.findAll(pageRequest);

        // then
        assertThat(posts.getTotalElements()).isEqualTo(25);
        assertThat(posts.getTotalPages()).isEqualTo(2);
        assertThat(posts.getContent()).hasSize(20);
    }

    @Test
    @DisplayName("사용자별 토론글 조회")
    void findByAuthor() {
        // given
        User anotherUser = User.builder()
                .username("anotheruser")
                .password("password")
                .name("다른유저")
                .email("another@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("anotheruser")
                .build();
        anotherUser = userRepository.save(anotherUser);

        discussionPostRepository.save(testPost);
        
        DiscussionPost anotherPost = DiscussionPost.builder()
                .title("다른 토론 주제")
                .content("다른 토론 내용")
                .author(anotherUser)
                .agreeCount(0L)
                .disagreeCount(0L)
                .build();
        discussionPostRepository.save(anotherPost);

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        // when
        Page<DiscussionPost> userPosts = discussionPostRepository.findByAuthor(testUser, pageRequest);

        // then
        assertThat(userPosts.getTotalElements()).isEqualTo(1);
        assertThat(userPosts.getContent().get(0).getAuthor()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("토론글 삭제 테스트")
    void deleteDiscussionPost() {
        // given
        DiscussionPost savedPost = discussionPostRepository.save(testPost);
        Long postId = savedPost.getId();

        // when
        discussionPostRepository.delete(savedPost);
        discussionPostRepository.flush();

        // then
        Optional<DiscussionPost> deletedPost = discussionPostRepository.findById(postId);
        assertThat(deletedPost).isEmpty();
    }

    @Test
    @DisplayName("토론글 수정 테스트")
    void updateDiscussionPost() {
        // given
        DiscussionPost savedPost = discussionPostRepository.save(testPost);

        // when
        savedPost.updatePost("수정된 제목", "수정된 내용");
        DiscussionPost updatedPost = discussionPostRepository.save(savedPost);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
    }
}
