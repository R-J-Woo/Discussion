package com.discussion.ryu.repository;

import com.discussion.ryu.entity.*;
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
@DisplayName("OpinionRepository 테스트")
class OpinionRepositoryTest {

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscussionPostRepository discussionPostRepository;

    private User testUser;
    private DiscussionPost testPost;
    private Opinion testOpinion;

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
        testPost = discussionPostRepository.save(testPost);

        testOpinion = Opinion.builder()
                .author(testUser)
                .discussionPost(testPost)
                .content("찬성 의견입니다")
                .stance(OpinionStance.AGREE)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();
    }

    @Test
    @DisplayName("의견 저장 테스트")
    void saveOpinion() {
        // when
        Opinion savedOpinion = opinionRepository.save(testOpinion);

        // then
        assertThat(savedOpinion.getId()).isNotNull();
        assertThat(savedOpinion.getContent()).isEqualTo("찬성 의견입니다");
        assertThat(savedOpinion.getStance()).isEqualTo(OpinionStance.AGREE);
        assertThat(savedOpinion.getAuthor()).isEqualTo(testUser);
        assertThat(savedOpinion.getDiscussionPost()).isEqualTo(testPost);
    }

    @Test
    @DisplayName("의견 ID로 조회 성공")
    void findById_Success() {
        // given
        Opinion savedOpinion = opinionRepository.save(testOpinion);

        // when
        Optional<Opinion> foundOpinion = opinionRepository.findById(savedOpinion.getId());

        // then
        assertThat(foundOpinion).isPresent();
        assertThat(foundOpinion.get().getContent()).isEqualTo("찬성 의견입니다");
    }

    @Test
    @DisplayName("의견 ID로 조회 실패")
    void findById_NotFound() {
        // when
        Optional<Opinion> foundOpinion = opinionRepository.findById(999L);

        // then
        assertThat(foundOpinion).isEmpty();
    }

    @Test
    @DisplayName("토론글별 의견 조회")
    void findByDiscussionPost() {
        // given
        for (int i = 0; i < 15; i++) {
            Opinion opinion = Opinion.builder()
                    .author(testUser)
                    .discussionPost(testPost)
                    .content("의견 " + i)
                    .stance(i % 2 == 0 ? OpinionStance.AGREE : OpinionStance.DISAGREE)
                    .likeCount(0L)
                    .dislikeCount(0L)
                    .build();
            opinionRepository.save(opinion);
        }

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // when
        Page<Opinion> opinions = opinionRepository.findByDiscussionPost(testPost, pageRequest);

        // then
        assertThat(opinions.getTotalElements()).isEqualTo(15);
        assertThat(opinions.getTotalPages()).isEqualTo(2);
        assertThat(opinions.getContent()).hasSize(10);
    }

    @Test
    @DisplayName("의견 삭제 테스트")
    void deleteOpinion() {
        // given
        Opinion savedOpinion = opinionRepository.save(testOpinion);
        Long opinionId = savedOpinion.getId();

        // when
        opinionRepository.delete(savedOpinion);
        opinionRepository.flush();

        // then
        Optional<Opinion> deletedOpinion = opinionRepository.findById(opinionId);
        assertThat(deletedOpinion).isEmpty();
    }

    @Test
    @DisplayName("의견 수정 테스트")
    void updateOpinion() {
        // given
        Opinion savedOpinion = opinionRepository.save(testOpinion);

        // when
        savedOpinion.updateOpinion("수정된 의견", OpinionStance.DISAGREE);
        Opinion updatedOpinion = opinionRepository.save(savedOpinion);

        // then
        assertThat(updatedOpinion.getContent()).isEqualTo("수정된 의견");
        assertThat(updatedOpinion.getStance()).isEqualTo(OpinionStance.DISAGREE);
    }

    @Test
    @DisplayName("다른 토론글의 의견 분리 조회")
    void findByDiscussionPost_Separated() {
        // given
        DiscussionPost anotherPost = DiscussionPost.builder()
                .title("다른 토론 주제")
                .content("다른 토론 내용")
                .author(testUser)
                .agreeCount(0L)
                .disagreeCount(0L)
                .build();
        anotherPost = discussionPostRepository.save(anotherPost);

        opinionRepository.save(testOpinion);

        Opinion anotherOpinion = Opinion.builder()
                .author(testUser)
                .discussionPost(anotherPost)
                .content("다른 토론글의 의견")
                .stance(OpinionStance.DISAGREE)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();
        opinionRepository.save(anotherOpinion);

        PageRequest pageRequest = PageRequest.of(0, 20);

        // when
        Page<Opinion> testPostOpinions = opinionRepository.findByDiscussionPost(testPost, pageRequest);
        Page<Opinion> anotherPostOpinions = opinionRepository.findByDiscussionPost(anotherPost, pageRequest);

        // then
        assertThat(testPostOpinions.getTotalElements()).isEqualTo(1);
        assertThat(anotherPostOpinions.getTotalElements()).isEqualTo(1);
        assertThat(testPostOpinions.getContent().get(0).getContent()).isEqualTo("찬성 의견입니다");
        assertThat(anotherPostOpinions.getContent().get(0).getContent()).isEqualTo("다른 토론글의 의견");
    }

    @Test
    @DisplayName("의견 좋아요/싫어요 카운트 증가 테스트")
    void incrementCounts() {
        // given
        Opinion savedOpinion = opinionRepository.save(testOpinion);

        // when
        savedOpinion.incrementLikeCount();
        savedOpinion.incrementLikeCount();
        savedOpinion.incrementDislikeCount();
        Opinion updatedOpinion = opinionRepository.save(savedOpinion);

        // then
        assertThat(updatedOpinion.getLikeCount()).isEqualTo(2L);
        assertThat(updatedOpinion.getDislikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("의견 좋아요/싫어요 카운트 감소 테스트")
    void decrementCounts() {
        // given
        Opinion opinionWithCounts = Opinion.builder()
                .author(testUser)
                .discussionPost(testPost)
                .content("카운트 테스트 의견")
                .stance(OpinionStance.AGREE)
                .likeCount(5L)
                .dislikeCount(3L)
                .build();
        Opinion savedOpinion = opinionRepository.save(opinionWithCounts);

        // when
        savedOpinion.decrementLikeCount();
        savedOpinion.decrementDislikeCount();
        Opinion updatedOpinion = opinionRepository.save(savedOpinion);

        // then
        assertThat(updatedOpinion.getLikeCount()).isEqualTo(4L);
        assertThat(updatedOpinion.getDislikeCount()).isEqualTo(2L);
    }
}
