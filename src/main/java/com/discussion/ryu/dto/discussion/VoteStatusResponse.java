package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionVote;
import com.discussion.ryu.entity.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class VoteStatusResponse {
    private boolean hasVoted;          // 투표 여부
    private VoteType voteType;         // 투표 타입 (없으면 null)
    private Long agreeCount;           // 총 찬성 수
    private Long disagreeCount;        // 총 반대 수
}
