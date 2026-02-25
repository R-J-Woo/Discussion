package com.discussion.ryu.dto.discussion;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class DiscussionPostCreateDto {

    @NotBlank(message = "제목은 필수입니다.")
    String title;

    @NotBlank(message = "내용은 필수입니다.")
    String content;
}
