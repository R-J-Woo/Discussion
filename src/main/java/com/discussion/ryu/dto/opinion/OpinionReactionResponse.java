package com.discussion.ryu.dto.opinion;

import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.ReactionType;
import lombok.Builder;
import lombok.Getter;

@Builder
public record OpinionReactionResponse(
        Long opinionId,
        Long likeCount,
        Long dislikeCount,
        ReactionType userReaction,
        String message
) {

    public static OpinionReactionResponse from(Opinion opinion, ReactionType userReaction, String message) {
        return new OpinionReactionResponse(
                opinion.getId(),
                opinion.getLikeCount(),
                opinion.getDislikeCount(),
                userReaction,
                message
        );
    }
}
