package com.discussion.ryu.dto.discussion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DiscussionPostUpdateDto {

    @NotBlank(message = "제목은 필수입니다.")
    String title;

    @NotBlank(message = "내용은 필수입니다.")
    String content;
}
