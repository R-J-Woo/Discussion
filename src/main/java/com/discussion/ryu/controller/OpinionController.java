package com.discussion.ryu.controller;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.dto.opinion.OpinionCreateDto;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.service.OpinionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussions/{postId}/opinions")
public class OpinionController {

    private final OpinionService opinionService;

    @PostMapping
    public ResponseEntity<ApiResponse<OpinionResponse>> createOpinion(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid @RequestBody OpinionCreateDto opinionCreateDto
    ) {
        OpinionResponse opinionResponse = opinionService.createOpinion(postId, user, opinionCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(opinionResponse, "의견 등록이 완료되었습니다.", HttpStatus.CREATED));
    }

}
