package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.DiscussionPostService;
import com.discussion.ryu.service.DiscussionVoteService;
import com.discussion.ryu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussions")
public class DiscussionPostController {

    private final DiscussionPostService discussionPostService;
    private final DiscussionVoteService discussionVoteService;

    // 토론글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DiscussionPostCreateDto discussionPostCreateDto
    ) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.createPost(user, discussionPostCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(discussionPostResponse, "토론글 등록이 완료되었습니다.", HttpStatus.CREATED));
    }

    // 토론글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscussionPostResponse>>> getAllPosts() {
        List<DiscussionPostResponse> discussionPostResponses = discussionPostService.getAllPosts();
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponses, "토론글 목록을 조회하였습니다.", HttpStatus.OK));
    }

    // 토론글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> getPost(@PathVariable Long postId) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponse, "토론글을 조회했습니다.", HttpStatus.OK));
    }

    // 내가 작성한 토론글 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DiscussionPostResponse>>> getMyPosts(@AuthenticationPrincipal User user) {
        List<DiscussionPostResponse> discussionPostResponses = discussionPostService.getMyPosts(user);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponses, "토론글을 조회했습니다.", HttpStatus.OK));
    }

    // 토론글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid @RequestBody DiscussionPostUpdateDto discussionPostUpdateDto
    ) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.updatePost(user, postId, discussionPostUpdateDto);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponse, "토론글이 수정되었습니다.", HttpStatus.OK));
    }

    // 토론글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        discussionPostService.deletePost(user, postId);
        return ResponseEntity.ok(ApiResponse.success(null, "토론글이 삭제되었습니다.", HttpStatus.OK));
    }

    // 토론글 투표 (찬성 / 반대)
    @PostMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> vote(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid@RequestBody VoteRequestDto voteRequestDto
    ) {
        VoteResponse voteResponse = discussionVoteService.vote(user, postId, voteRequestDto);
        return ResponseEntity.ok(ApiResponse.success(voteResponse, "투표가 완료되었습니다.", HttpStatus.OK));
    }

    // 토론글 투표 취소
    @DeleteMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<Void>> cancelVote(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        discussionVoteService.cancelVote(postId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "투표가 취소되었습니다.", HttpStatus.OK));
    }

    // 내 투표 상태 확인
    @GetMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<VoteStatusResponse>> getVoteStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        VoteStatusResponse response = discussionVoteService.getVoteStatus(postId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "투표 상태를 조회했습니다.", HttpStatus.OK));
    }
}
