package com.discussion.ryu.dto.opinion;

import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.OpinionStance;

import java.time.LocalDateTime;

public record OpinionResponse(
        Long id,
        String content,
        OpinionStance opinionStance,
        String authorName,
        Long likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static OpinionResponse from(Opinion opinion) {
        return new OpinionResponse(
                opinion.getId(),
                opinion.getContent(),
                opinion.getStance(),
                opinion.getAuthor().getName(),
                opinion.getLikeCount(),
                opinion.getCreatedAt(),
                opinion.getUpdatedAt()
        );
    }
}
