package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.discussion.DiscussionPostCreateDto;
import com.discussion.ryu.dto.discussion.DiscussionPostResponse;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.DiscussionPostService;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscussionPostResponse>>> getAllPosts() {
        List<DiscussionPostResponse> discussionPostResponses = discussionPostService.getAllPosts();
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponses, "토론글 목록을 조회하였습니다.", HttpStatus.OK));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> getPost(@PathVariable Long postId) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponse, "토론글을 조회했습니다.", HttpStatus.OK));
    }
}
