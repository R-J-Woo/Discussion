package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.DiscussionPostService;
import com.discussion.ryu.service.DiscussionVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "토론글", description = "토론글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussions")
public class DiscussionPostController {

    private final DiscussionPostService discussionPostService;
    private final DiscussionVoteService discussionVoteService;

    @Operation(summary = "토론글 등록", description = "새로운 토론 주제를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "토론글 등록 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody DiscussionPostCreateDto discussionPostCreateDto
    ) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.createPost(user, discussionPostCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(discussionPostResponse, "토론글 등록이 완료되었습니다.", HttpStatus.CREATED));
    }

    @Operation(summary = "토론글 목록 조회", description = "모든 토론글을 페이징하여 조회합니다. (인증 불필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DiscussionPostResponse>>> getAllPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<DiscussionPostResponse> discussionPostResponses = discussionPostService.getAllPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponses, "토론글 목록을 조회하였습니다.", HttpStatus.OK));
    }

    @Operation(summary = "토론글 상세 조회", description = "특정 토론글의 상세 정보를 조회합니다. (인증 불필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> getPost(
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "의견 페이지 번호 (0부터 시작)", example = "0")
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.getPost(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponse, "토론글을 조회했습니다.", HttpStatus.OK));
    }

    @Operation(summary = "내가 작성한 토론글 목록 조회", description = "로그인한 사용자가 작성한 토론글을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<DiscussionPostResponse>>> getMyPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<DiscussionPostResponse> discussionPostResponses = discussionPostService.getMyPosts(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponses, "토론글을 조회했습니다.", HttpStatus.OK));
    }

    @Operation(summary = "토론글 수정", description = "작성한 토론글을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (작성자가 아님)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<DiscussionPostResponse>> updatePost(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId,
            @Valid @RequestBody DiscussionPostUpdateDto discussionPostUpdateDto
    ) {
        DiscussionPostResponse discussionPostResponse = discussionPostService.updatePost(user, postId, discussionPostUpdateDto);
        return ResponseEntity.ok(ApiResponse.success(discussionPostResponse, "토론글이 수정되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "토론글 삭제", description = "작성한 토론글을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (작성자가 아님)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId
    ) {
        discussionPostService.deletePost(user, postId);
        return ResponseEntity.ok(ApiResponse.success(null, "토론글이 삭제되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "토론글 투표", description = "토론글에 찬성 또는 반대 투표를 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "투표 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 투표함)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @PostMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> vote(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId,
            @Valid@RequestBody VoteRequestDto voteRequestDto
    ) {
        VoteResponse voteResponse = discussionVoteService.vote(user, postId, voteRequestDto);
        return ResponseEntity.ok(ApiResponse.success(voteResponse, "투표가 완료되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "토론글 투표 취소", description = "자신이 한 투표를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "투표 취소 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "투표하지 않은 토론글"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @DeleteMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<Void>> cancelVote(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId
    ) {
        discussionVoteService.cancelVote(postId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "투표가 취소되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "내 투표 상태 확인", description = "특정 토론글에 대한 내 투표 상태를 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토론글을 찾을 수 없음"
            )
    })
    @GetMapping("/{postId}/vote")
    public ResponseEntity<ApiResponse<VoteStatusResponse>> getVoteStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId
    ) {
        VoteStatusResponse response = discussionVoteService.getVoteStatus(postId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "투표 상태를 조회했습니다.", HttpStatus.OK));
    }
}
