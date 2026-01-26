package com.discussion.ryu.dto.opinion;

import com.discussion.ryu.entity.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpinionReactionRequestDto {

    @NotNull(message = "반응 타입은 필수입니다.")
    private ReactionType reactionType;
}
