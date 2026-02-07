package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.DiscussionPostCreateDto;
import com.discussion.ryu.dto.discussion.DiscussionPostResponse;
import com.discussion.ryu.dto.discussion.DiscussionPostUpdateDto;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostRepository;
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
@DisplayName("DiscussionPostService 테스트")
class DiscussionPostServiceTest {

    @Mock
    private DiscussionPostRepository discussionPostRepository;

    @Mock
    private OpinionService opinionService;

    @InjectMocks
    private DiscussionPostService discussionPostService;

    private User testUser;
    private User anotherUser;
    private DiscussionPost testPost;
    private DiscussionPostCreateDto createDto;

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

        createDto = DiscussionPostCreateDto.builder()
                .title("토론 주제입니다")
                .content("토론 내용입니다")
                .build();
    }

    @Test
    @DisplayName("토론글 생성 성공")
    void createPost_Success() {
        // given
        given(discussionPostRepository.save(any(DiscussionPost.class))).willReturn(testPost);

        // when
        DiscussionPostResponse response = discussionPostService.createPost(testUser, createDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("토론 주제입니다");
        assertThat(response.content()).isEqualTo("토론 내용입니다");
        verify(discussionPostRepository, times(1)).save(any(DiscussionPost.class));
    }

    @Test
    @DisplayName("모든 토론글 조회 성공")
    void getAllPosts_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<DiscussionPost> page = new PageImpl<>(List.of(testPost));
        given(discussionPostRepository.findAllWithAuthor(pageable)).willReturn(page);

        // when
        Page<DiscussionPostResponse> result = discussionPostService.getAllPosts(pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).title()).isEqualTo("토론 주제입니다");
        verify(discussionPostRepository, times(1)).findAllWithAuthor(pageable);
    }

    @Test
    @DisplayName("토론글 상세 조회 성공")
    void getPost_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));
        given(opinionService.getOpinionsByPost(any(DiscussionPost.class), any(Pageable.class)))
                .willReturn(Page.empty());

        // when
        DiscussionPostResponse response = discussionPostService.getPost(1L, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("토론 주제입니다");
        verify(discussionPostRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("토론글 상세 조회 실패 - 존재하지 않는 게시글")
    void getPost_NotFound() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        given(discussionPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> discussionPostService.getPost(999L, pageable))
                .isInstanceOf(DiscussionPostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 토론글입니다.");
    }

    @Test
    @DisplayName("내 토론글 목록 조회 성공")
    void getMyPosts_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<DiscussionPost> page = new PageImpl<>(List.of(testPost));
        given(discussionPostRepository.findByAuthor(testUser, pageable)).willReturn(page);

        // when
        Page<DiscussionPostResponse> result = discussionPostService.getMyPosts(testUser, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).authorName()).isEqualTo("테스트유저");
        verify(discussionPostRepository, times(1)).findByAuthor(testUser, pageable);
    }

    @Test
    @DisplayName("토론글 수정 성공")
    void updatePost_Success() {
        // given
        DiscussionPostUpdateDto updateDto = DiscussionPostUpdateDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));
        given(discussionPostRepository.save(any(DiscussionPost.class))).willReturn(testPost);

        // when
        DiscussionPostResponse response = discussionPostService.updatePost(testUser, 1L, updateDto);

        // then
        assertThat(response).isNotNull();
        verify(discussionPostRepository, times(1)).save(any(DiscussionPost.class));
    }

    @Test
    @DisplayName("토론글 수정 실패 - 존재하지 않는 게시글")
    void updatePost_NotFound() {
        // given
        DiscussionPostUpdateDto updateDto = DiscussionPostUpdateDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(discussionPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> discussionPostService.updatePost(testUser, 999L, updateDto))
                .isInstanceOf(DiscussionPostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 토론글입니다.");
    }

    @Test
    @DisplayName("토론글 수정 실패 - 작성자가 아님")
    void updatePost_NotAuthor() {
        // given
        DiscussionPostUpdateDto updateDto = DiscussionPostUpdateDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> discussionPostService.updatePost(anotherUser, 1L, updateDto))
                .isInstanceOf(UserNotAuthorException.class)
                .hasMessageContaining("본인이 작성한 토론글만 수정할 수 있습니다.");

        verify(discussionPostRepository, never()).save(any(DiscussionPost.class));
    }

    @Test
    @DisplayName("토론글 삭제 성공")
    void deletePost_Success() {
        // given
        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when
        discussionPostService.deletePost(testUser, 1L);

        // then
        verify(discussionPostRepository, times(1)).delete(testPost);
    }

    @Test
    @DisplayName("토론글 삭제 실패 - 존재하지 않는 게시글")
    void deletePost_NotFound() {
        // given
        given(discussionPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> discussionPostService.deletePost(testUser, 999L))
                .isInstanceOf(DiscussionPostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 토론글입니다.");
    }

    @Test
    @DisplayName("토론글 삭제 실패 - 작성자가 아님")
    void deletePost_NotAuthor() {
        // given
        given(discussionPostRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> discussionPostService.deletePost(anotherUser, 1L))
                .isInstanceOf(UserNotAuthorException.class)
                .hasMessageContaining("본인이 작성한 토론글만 삭제할 수 있습니다.");

        verify(discussionPostRepository, never()).delete(testPost);
    }
}
