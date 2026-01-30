package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.entity.DiscussionVote;
import com.discussion.ryu.entity.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VoteResponse(
        Long voteId,
        Long postId,
        VoteType voteType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VoteResponse from(DiscussionVote vote, Long discussionId) {
        return new VoteResponse(
                vote.getId(),
                discussionId,
                vote.getVoteType(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }
}
