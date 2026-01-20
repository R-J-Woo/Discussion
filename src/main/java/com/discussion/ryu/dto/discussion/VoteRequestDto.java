package com.discussion.ryu.dto.discussion;

import com.discussion.ryu.entity.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VoteRequestDto {

    @NotNull(message = "투표 타입은 필수입니다.")
    private VoteType voteType;
}
