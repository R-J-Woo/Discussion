package com.discussion.ryu.dto.opinion;

import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.OpinionStance;
import com.discussion.ryu.entity.ReactionType;

import java.time.LocalDateTime;

public record OpinionResponse(
        Long id,
        String content,
        OpinionStance opinionStance,
        String authorName,
        Long likeCount,
        Long dislikeCount,
        ReactionType userReaction,
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
                opinion.getDislikeCount(),
                null,
                opinion.getCreatedAt(),
                opinion.getUpdatedAt()
        );
    }

    public static OpinionResponse from(Opinion opinion, ReactionType reactionType) {
        return new OpinionResponse(
                opinion.getId(),
                opinion.getContent(),
                opinion.getStance(),
                opinion.getAuthor().getName(),
                opinion.getLikeCount(),
                opinion.getDislikeCount(),
                reactionType,
                opinion.getCreatedAt(),
                opinion.getUpdatedAt()
        );
    }
}
