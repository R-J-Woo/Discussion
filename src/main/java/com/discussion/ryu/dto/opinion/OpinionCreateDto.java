package com.discussion.ryu.dto.opinion;

import com.discussion.ryu.entity.OpinionStance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OpinionCreateDto {

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 10, max = 2000, message = "내용은 10자 이상 2000자 이하여야 합니다.")
    String content;

    @NotNull(message = "입장은 필수입니다.")
    OpinionStance opinionStance;
}
