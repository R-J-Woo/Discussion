package com.discussion.ryu.controller;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.opinion.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.OpinionStance;
import com.discussion.ryu.entity.ReactionType;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.opinion.OpinionNotFoundException;
import com.discussion.ryu.service.CustomUserDetailsService;
import com.discussion.ryu.service.OpinionReactionService;
import com.discussion.ryu.service.OpinionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpinionController.class)
@ActiveProfiles("test")
@DisplayName("OpinionController 테스트")
class OpinionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OpinionService opinionService;

    @MockitoBean
    private OpinionReactionService opinionReactionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private OpinionCreateDto createDto;
    private OpinionResponse opinionResponse;

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

        createDto = OpinionCreateDto.builder()
                .content("찬성 의견입니다")
                .opinionStance(OpinionStance.AGREE)
                .build();

        opinionResponse = OpinionResponse.builder()
                .id(1L)
                .content("찬성 의견입니다")
                .opinionStance(OpinionStance.AGREE)
                .authorName("테스트유저")
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("의견 등록 성공")
    @WithMockUser
    void createOpinion_Success() throws Exception {
        // given
        given(opinionService.createOpinion(eq(1L), any(User.class), any(OpinionCreateDto.class)))
                .willReturn(opinionResponse);

        // when & then
        mockMvc.perform(post("/api/discussions/1/opinions")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("찬성 의견입니다"))
                .andExpect(jsonPath("$.data.stance").value("AGREE"))
                .andExpect(jsonPath("$.message").value("의견 등록이 완료되었습니다."));

        verify(opinionService, times(1)).createOpinion(eq(1L), any(User.class), any(OpinionCreateDto.class));
    }

    @Test
    @DisplayName("의견 등록 실패 - 유효성 검증 실패 (빈 내용)")
    @WithMockUser
    void createOpinion_ValidationFail() throws Exception {
        // given
        OpinionCreateDto invalidDto = OpinionCreateDto.builder()
                .content("")
                .opinionStance(OpinionStance.AGREE)
                .build();

        // when & then
        mockMvc.perform(post("/api/discussions/1/opinions")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(opinionService, never()).createOpinion(any(Long.class), any(User.class), any(OpinionCreateDto.class));
    }

    @Test
    @DisplayName("의견 수정 성공")
    @WithMockUser
    void updateOpinion_Success() throws Exception {
        // given
        OpinionUpdateDto updateDto = OpinionUpdateDto.builder()
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .build();

        OpinionResponse updatedResponse = OpinionResponse.builder()
                .id(1L)
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .authorName("테스트유저")
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(opinionService.updateOpinion(eq(1L), any(User.class), any(OpinionUpdateDto.class)))
                .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/discussions/1/opinions/1")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("수정된 의견입니다"))
                .andExpect(jsonPath("$.data.stance").value("DISAGREE"))
                .andExpect(jsonPath("$.message").value("의견이 수정되었습니다."));
    }

    @Test
    @DisplayName("의견 수정 실패 - 존재하지 않는 의견")
    @WithMockUser
    void updateOpinion_NotFound() throws Exception {
        // given
        OpinionUpdateDto updateDto = OpinionUpdateDto.builder()
                .content("수정된 의견입니다")
                .opinionStance(OpinionStance.DISAGREE)
                .build();

        given(opinionService.updateOpinion(eq(999L), any(User.class), any(OpinionUpdateDto.class)))
                .willThrow(new OpinionNotFoundException("의견을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(put("/api/discussions/1/opinions/999")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("의견 삭제 성공")
    @WithMockUser
    void deleteOpinion_Success() throws Exception {
        // given
        doNothing().when(opinionService).deleteOpinion(eq(1L), any(User.class));

        // when & then
        mockMvc.perform(delete("/api/discussions/1/opinions/1")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("의견이 삭제되었습니다."));

        verify(opinionService, times(1)).deleteOpinion(eq(1L), any(User.class));
    }

    @Test
    @DisplayName("의견 삭제 실패 - 존재하지 않는 의견")
    @WithMockUser
    void deleteOpinion_NotFound() throws Exception {
        // given
        doThrow(new OpinionNotFoundException("의견을 찾을 수 없습니다."))
                .when(opinionService).deleteOpinion(eq(999L), any(User.class));

        // when & then
        mockMvc.perform(delete("/api/discussions/1/opinions/999")
                        .with(user(testUser))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("의견 반응 추가 성공")
    @WithMockUser
    void toggleReaction_Success() throws Exception {
        // given
        OpinionReactionRequestDto requestDto = OpinionReactionRequestDto.builder()
                .reactionType(ReactionType.LIKE)
                .build();

        OpinionReactionResponse reactionResponse = OpinionReactionResponse.builder()
                .opinionId(1L)
                .userReaction(ReactionType.LIKE)
                .likeCount(1L)
                .dislikeCount(0L)
                .build();

        given(opinionReactionService.toggleReaction(eq(1L), any(User.class), any(OpinionReactionRequestDto.class)))
                .willReturn(reactionResponse);

        // when & then
        mockMvc.perform(post("/api/discussions/1/opinions/1/reaction")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reactionType").value("LIKE"))
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.message").value("사용자 의견 반영에 성공하였습니다."));
    }

    @Test
    @DisplayName("의견 반응 추가 실패 - 존재하지 않는 의견")
    @WithMockUser
    void toggleReaction_NotFound() throws Exception {
        // given
        OpinionReactionRequestDto requestDto = OpinionReactionRequestDto.builder()
                .reactionType(ReactionType.LIKE)
                .build();

        given(opinionReactionService.toggleReaction(eq(999L), any(User.class), any(OpinionReactionRequestDto.class)))
                .willThrow(new OpinionNotFoundException("의견을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/api/discussions/1/opinions/999/reaction")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
