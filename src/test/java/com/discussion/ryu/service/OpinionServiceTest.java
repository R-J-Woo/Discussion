package com.discussion.ryu.service;

import com.discussion.ryu.dto.opinion.OpinionCreateDto;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.dto.opinion.OpinionUpdateDto;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.exception.opinion.OpinionNotFoundException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.OpinionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpinionService 테스트")
class OpinionServiceTest {

    @Mock
    private OpinionRepository opinionRepository;

    @Mock
    private DiscussionPostRepository discussionPostRepository;

    @InjectMocks
    private OpinionService opinionService;

    private User testUser;
    private User anotherUser;
    private DiscussionPost testPost;
    private Opinion testOpinion;
    private OpinionCreateDto createDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .password("encodedPassword")
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("testuser")
                .build();

        anotherUser = User.builder()
                .userId(2L)
                .username("anotheruser")
                .password("encodedPassword")
                .name("다른유저")
                .email("another@example.com")
                .provider(AuthProvider.LOCAL)
                .providerId("anotheruser")
                .build();

        testPost = DiscussionPost.builder()
                .id(1L)
                .title("토론 주제입니다")
                .content("토론 내용입니다")
                .author(testUser)
                .agreeCount(0L)
                .disagreeCount(0L)
                .build();

        testOpinion = Opinion.builder()
                .id(1L)
                .author(testUser)
                .discussionPost(testPost)
                .content("찬성 의견입니다")
                .stance(OpinionStance.AGREE)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();

        createDto = OpinionCreateDto.builder()
                .content("찬성 의견입니다")
                .opinionStance(OpinionStance.AGREE)
                .build();
    }

    @Test
    @DisplayName("의견 생성 성공")
    void createOpinion_Success() {
        // given
        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));
        given(opinionRepository.save(any(Opinion.class))).willReturn(testOpinion);

        // when
        OpinionResponse response = opinionService.createOpinion(1L, testUser, createDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("찬성 의견입니다");
        assertThat(response.opinionStance()).isEqualTo(OpinionStance.AGREE);
        verify(opinionRepository, times(1)).save(any(Opinion.class));
    }

    @Test
    @DisplayName("의견 생성 실패 - 존재하지 않는 토론글")
    void createOpinion_PostNotFound() {
        // given
        given(discussionPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> opinionService.createOpinion(999L, testUser, createDto))
                .isInstanceOf(DiscussionPostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 토론글입니다.");

        verify(opinionRepository, never()).save(any(Opinion.class));
    }

    @Test
    @DisplayName("토론글의 의견 목록 조회 성공")
    void getOpinionsByPost_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Opinion> page = new PageImpl<>(List.of(testOpinion));
        given(opinionRepository.findByDiscussionPost(testPost, pageable)).willReturn(page);

        // when
        Page<OpinionResponse> result = opinionService.getOpinionsByPost(testPost, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).content()).isEqualTo("찬성 의견입니다");
        verify(opinionRepository, times(1)).findByDiscussionPost(testPost, pageable);
    }

    @Test
    @DisplayName("의견 수정 성공")
    void updateOpinion_Success() {
        // given
        OpinionUpdateDto updateDto = OpinionUpdateDto.builder()
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .build();

        given(opinionRepository.findById(1L)).willReturn(Optional.of(testOpinion));
        given(opinionRepository.save(any(Opinion.class))).willReturn(testOpinion);

        // when
        OpinionResponse response = opinionService.updateOpinion(1L, testUser, updateDto);

        // then
        assertThat(response).isNotNull();
        verify(opinionRepository, times(1)).save(any(Opinion.class));
    }

    @Test
    @DisplayName("의견 수정 실패 - 존재하지 않는 의견")
    void updateOpinion_NotFound() {
        // given
        OpinionUpdateDto updateDto = OpinionUpdateDto.builder()
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .build();

        given(opinionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> opinionService.updateOpinion(999L, testUser, updateDto))
                .isInstanceOf(OpinionNotFoundException.class)
                .hasMessageContaining("존재하지 않는 의견입니다.");
    }

    @Test
    @DisplayName("의견 수정 실패 - 작성자가 아님")
    void updateOpinion_NotAuthor() {
        // given
        OpinionUpdateDto updateDto = OpinionUpdateDto.builder()
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .build();

        given(opinionRepository.findById(1L)).willReturn(Optional.of(testOpinion));

        // when & then
        assertThatThrownBy(() -> opinionService.updateOpinion(1L, anotherUser, updateDto))
                .isInstanceOf(UserNotAuthorException.class)
                .hasMessageContaining("본인이 작성한 의견만 수정할 수 있습니다.");

        verify(opinionRepository, never()).save(any(Opinion.class));
    }

    @Test
    @DisplayName("의견 삭제 성공")
    void deleteOpinion_Success() {
        // given
        given(opinionRepository.findById(1L)).willReturn(Optional.of(testOpinion));

        // when
        opinionService.deleteOpinion(1L, testUser);

        // then
        verify(opinionRepository, times(1)).delete(testOpinion);
    }

    @Test
    @DisplayName("의견 삭제 실패 - 존재하지 않는 의견")
    void deleteOpinion_NotFound() {
        // given
        given(opinionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> opinionService.deleteOpinion(999L, testUser))
                .isInstanceOf(OpinionNotFoundException.class)
                .hasMessageContaining("존재하지 않는 의견입니다.");
    }

    @Test
    @DisplayName("의견 삭제 실패 - 작성자가 아님")
    void deleteOpinion_NotAuthor() {
        // given
        given(opinionRepository.findById(1L)).willReturn(Optional.of(testOpinion));

        // when & then
        assertThatThrownBy(() -> opinionService.deleteOpinion(1L, anotherUser))
                .isInstanceOf(UserNotAuthorException.class)
                .hasMessageContaining("본인이 작성한 의견만 삭제할 수 있습니다.");

        verify(opinionRepository, never()).delete(testOpinion);
    }
}
