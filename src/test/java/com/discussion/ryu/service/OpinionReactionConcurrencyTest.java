package com.discussion.ryu.service;

import com.discussion.ryu.dto.opinion.OpinionReactionRequestDto;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.OpinionReactionRepository;
import com.discussion.ryu.repository.OpinionRepository;
import com.discussion.ryu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpinionReaction의 동시성 문제를 테스트하는 클래스
 * 
 * 이 테스트는 멀티스레드 환경에서 동시에 여러 사용자가 좋아요를 누를 때
 * likeCount가 정확하게 증가하는지 확인합니다.
 */
@SpringBootTest
class OpinionReactionConcurrencyTest {

    @Autowired
    private OpinionReactionService opinionReactionService;

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscussionPostRepository discussionPostRepository;

    @Autowired
    private OpinionReactionRepository opinionReactionRepository;

    private Opinion testOpinion;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        opinionReactionRepository.deleteAll();
        opinionRepository.deleteAll();
        discussionPostRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        User author = User.builder()
                .username("author")
                .email("author@test.com")
                .password("password")
                .name("작성자")
                .provider(AuthProvider.LOCAL)
                .providerId("local_author")
                .build();
        userRepository.save(author);

        // 테스트용 토론 글 생성
        DiscussionPost post = DiscussionPost.builder()
                .author(author)
                .title("테스트 토론")
                .content("테스트 내용")
                .build();
        discussionPostRepository.save(post);

        // 테스트용 의견 생성
        testOpinion = Opinion.builder()
                .author(author)
                .discussionPost(post)
                .content("테스트 의견")
                .stance(OpinionStance.AGREE)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();
        testOpinion = opinionRepository.save(testOpinion);

        // 테스트용 유저 100명 생성
        testUsers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@test.com")
                    .password("password")
                    .name("유저" + i)
                    .provider(AuthProvider.LOCAL)
                    .providerId("local_user" + i)
                    .build();
            testUsers.add(userRepository.save(user));
        }
    }

    @Test
    @DisplayName("동시에 100명이 좋아요를 누르면 likeCount는 100이 되어야 한다")
    void concurrentLikeTest() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    OpinionReactionRequestDto requestDto = OpinionReactionRequestDto.builder()
                            .reactionType(ReactionType.LIKE)
                            .build();
                    
                    opinionReactionService.toggleReaction(
                            testOpinion.getId(),
                            testUsers.get(index),
                            requestDto
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Thread " + index + " failed: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Opinion result = opinionRepository.findById(testOpinion.getId()).orElseThrow();
        long actualReactionCount = opinionReactionRepository.countByOpinion(result);

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("성공한 요청: " + successCount.get());
        System.out.println("실패한 요청: " + failCount.get());
        System.out.println("실제 DB의 Opinion likeCount: " + result.getLikeCount());
        System.out.println("실제 DB의 OpinionReaction 개수: " + actualReactionCount);
        System.out.println("예상 likeCount: " + threadCount);

        // 이 assertion은 현재 코드에서는 실패할 가능성이 높습니다
        // Lost Update 문제로 인해 likeCount < 100 이 될 수 있습니다
        assertThat(result.getLikeCount())
                .as("동시에 %d명이 좋아요를 눌렀을 때 likeCount", threadCount)
                .isEqualTo((long) threadCount);

        assertThat(actualReactionCount)
                .as("OpinionReaction 테이블의 실제 레코드 수")
                .isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시에 50명이 좋아요, 50명이 싫어요를 누르는 경우")
    void concurrentLikeAndDislikeTest() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    ReactionType reactionType = (index < 50) ? ReactionType.LIKE : ReactionType.DISLIKE;
                    OpinionReactionRequestDto requestDto = OpinionReactionRequestDto.builder()
                            .reactionType(reactionType)
                            .build();
                    
                    opinionReactionService.toggleReaction(
                            testOpinion.getId(),
                            testUsers.get(index),
                            requestDto
                    );
                } catch (Exception e) {
                    System.err.println("Thread " + index + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Opinion result = opinionRepository.findById(testOpinion.getId()).orElseThrow();

        System.out.println("=== 좋아요/싫어요 혼합 테스트 결과 ===");
        System.out.println("실제 likeCount: " + result.getLikeCount());
        System.out.println("실제 dislikeCount: " + result.getDislikeCount());
        System.out.println("예상 likeCount: 50");
        System.out.println("예상 dislikeCount: 50");

        // 이 테스트도 동시성 문제로 인해 실패할 수 있습니다
        assertThat(result.getLikeCount()).isEqualTo(50L);
        assertThat(result.getDislikeCount()).isEqualTo(50L);
    }
}
