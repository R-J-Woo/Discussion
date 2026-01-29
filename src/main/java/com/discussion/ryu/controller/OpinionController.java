package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.opinion.*;
import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.OpinionReactionService;
import com.discussion.ryu.service.OpinionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "의견", description = "토론글 의견 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussions/{postId}/opinions")
public class OpinionController {

    private final OpinionService opinionService;
    private final OpinionReactionService opinionReactionService;

    @Operation(summary = "의견 등록", description = "토론글에 찬성 또는 반대 의견을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "의견 등록 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 의견 작성함)"
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
    @PostMapping
    public ResponseEntity<ApiResponse<OpinionResponse>> createOpinion(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "토론글 ID", required = true) @PathVariable Long postId,
            @Valid @RequestBody OpinionCreateDto opinionCreateDto
    ) {
        OpinionResponse opinionResponse = opinionService.createOpinion(postId, user, opinionCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(opinionResponse, "의견 등록이 완료되었습니다.", HttpStatus.CREATED));
    }

    @Operation(summary = "의견 수정", description = "작성한 의견을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "의견 수정 성공"
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
                    description = "의견을 찾을 수 없음"
            )
    })
    @PutMapping("/{opinionId}")
    public ResponseEntity<ApiResponse<OpinionResponse>> updateOpinion(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "의견 ID", required = true) @PathVariable Long opinionId,
            @Valid @RequestBody OpinionUpdateDto opinionUpdateDto
    ) {
        OpinionResponse opinionResponse = opinionService.updateOpinion(opinionId, user, opinionUpdateDto);
        return ResponseEntity.ok(ApiResponse.success(opinionResponse, "의견이 수정되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "의견 삭제", description = "작성한 의견을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "의견 삭제 성공"
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
                    description = "의견을 찾을 수 없음"
            )
    })
    @DeleteMapping("/{opinionId}")
    public ResponseEntity<ApiResponse<Void>> deleteOpinion(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "의견 ID", required = true) @PathVariable Long opinionId
    ) {
        opinionService.deleteOpinion(opinionId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "의견이 삭제되었습니다.", HttpStatus.OK));
    }

    @Operation(summary = "의견 반응", description = "다른 사용자의 의견에 좋아요/싫어요 반응을 추가하거나 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "반응 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (자신의 의견에 반응 불가)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "의견을 찾을 수 없음"
            )
    })
    @PostMapping("/{opinionId}/reaction")
    public ResponseEntity<ApiResponse<OpinionReactionResponse>> toggleReaction(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "의견 ID", required = true) @PathVariable Long opinionId,
            @Valid @RequestBody OpinionReactionRequestDto opinionReactionRequestDto
    ) {
        OpinionReactionResponse response = opinionReactionService.toggleReaction(opinionId, user, opinionReactionRequestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 의견 반영에 성공하였습니다.", HttpStatus.OK));
    }
}
