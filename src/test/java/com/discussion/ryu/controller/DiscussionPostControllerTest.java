package com.discussion.ryu.controller;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.service.CustomUserDetailsService;
import com.discussion.ryu.service.DiscussionPostService;
import com.discussion.ryu.service.DiscussionVoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscussionPostController.class)
@ActiveProfiles("test")
@DisplayName("DiscussionPostController 테스트")
class DiscussionPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiscussionPostService discussionPostService;

    @MockitoBean
    private DiscussionVoteService discussionVoteService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private DiscussionPostCreateDto createDto;
    private DiscussionPostResponse postResponse;

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

        createDto = DiscussionPostCreateDto.builder()
                .title("토론 주제입니다")
                .content("토론 내용입니다")
                .build();

        postResponse = DiscussionPostResponse.builder()
                .id(1L)
                .title("토론 주제입니다")
                .content("토론 내용입니다")
                .authorName("테스트유저")
                .agreeCount(0L)
                .disagreeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("토론글 등록 성공")
    @WithMockUser
    void createPost_Success() throws Exception {
        // given
        given(discussionPostService.createPost(any(User.class), any(DiscussionPostCreateDto.class)))
                .willReturn(postResponse);

        // when & then
        mockMvc.perform(post("/api/discussions")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("토론 주제입니다"))
                .andExpect(jsonPath("$.data.content").value("토론 내용입니다"))
                .andExpect(jsonPath("$.message").value("토론글 등록이 완료되었습니다."));

        verify(discussionPostService, times(1)).createPost(any(User.class), any(DiscussionPostCreateDto.class));
    }

    @Test
    @DisplayName("토론글 등록 실패 - 유효성 검증 실패 (빈 제목)")
    @WithMockUser
    void createPost_ValidationFail() throws Exception {
        // given
        DiscussionPostCreateDto invalidDto = DiscussionPostCreateDto.builder()
                .title("")
                .content("토론 내용입니다")
                .build();

        // when & then
        mockMvc.perform(post("/api/discussions")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(discussionPostService, never()).createPost(any(User.class), any(DiscussionPostCreateDto.class));
    }

    @Test
    @DisplayName("토론글 목록 조회 성공")
    void getAllPosts_Success() throws Exception {
        // given
        Page<DiscussionPostResponse> page = new PageImpl<>(List.of(postResponse), PageRequest.of(0, 20), 1);
        given(discussionPostService.getAllPosts(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/discussions")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("토론 주제입니다"))
                .andExpect(jsonPath("$.message").value("토론글 목록을 조회하였습니다."));
    }

    @Test
    @DisplayName("토론글 상세 조회 성공")
    void getPost_Success() throws Exception {
        // given
        given(discussionPostService.getPost(eq(1L), any())).willReturn(postResponse);

        // when & then
        mockMvc.perform(get("/api/discussions/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("토론 주제입니다"))
                .andExpect(jsonPath("$.message").value("토론글을 조회했습니다."));
    }

    @Test
    @DisplayName("토론글 상세 조회 실패 - 존재하지 않는 게시글")
    void getPost_NotFound() throws Exception {
        // given
        given(discussionPostService.getPost(eq(999L), any()))
                .willThrow(new DiscussionPostNotFoundException("토론글을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/discussions/999")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내가 작성한 토론글 목록 조회 성공")
    @WithMockUser
    void getMyPosts_Success() throws Exception {
        // given
        Page<DiscussionPostResponse> page = new PageImpl<>(List.of(postResponse), PageRequest.of(0, 20), 1);
        given(discussionPostService.getMyPosts(any(User.class), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/discussions/my")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("토론 주제입니다"))
                .andExpect(jsonPath("$.message").value("토론글을 조회했습니다."));
    }

    @Test
    @DisplayName("토론글 수정 성공")
    @WithMockUser
    void updatePost_Success() throws Exception {
        // given
        DiscussionPostUpdateDto updateDto = DiscussionPostUpdateDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        DiscussionPostResponse updatedResponse = DiscussionPostResponse.builder()
                .id(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .authorName("테스트유저")
                .agreeCount(0L)
                .disagreeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(discussionPostService.updatePost(any(User.class), eq(1L), any(DiscussionPostUpdateDto.class)))
                .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/discussions/1")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.message").value("토론글이 수정되었습니다."));
    }

    @Test
    @DisplayName("토론글 수정 실패 - 작성자가 아님")
    @WithMockUser
    void updatePost_NotAuthor() throws Exception {
        // given
        DiscussionPostUpdateDto updateDto = DiscussionPostUpdateDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(discussionPostService.updatePost(any(User.class), eq(1L), any(DiscussionPostUpdateDto.class)))
                .willThrow(new UserNotAuthorException("작성자만 수정할 수 있습니다."));

        // when & then
        mockMvc.perform(put("/api/discussions/1")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("토론글 삭제 성공")
    @WithMockUser
    void deletePost_Success() throws Exception {
        // given
        doNothing().when(discussionPostService).deletePost(any(User.class), eq(1L));

        // when & then
        mockMvc.perform(delete("/api/discussions/1")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토론글이 삭제되었습니다."));

        verify(discussionPostService, times(1)).deletePost(any(User.class), eq(1L));
    }

    @Test
    @DisplayName("토론글 삭제 실패 - 작성자가 아님")
    @WithMockUser
    void deletePost_NotAuthor() throws Exception {
        // given
        doThrow(new UserNotAuthorException("작성자만 삭제할 수 있습니다."))
                .when(discussionPostService).deletePost(any(User.class), eq(1L));

        // when & then
        mockMvc.perform(delete("/api/discussions/1")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
