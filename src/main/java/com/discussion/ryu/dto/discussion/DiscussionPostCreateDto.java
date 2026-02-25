package com.discussion.ryu.dto.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class DiscussionPostCreateDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 5, max = 200, message = "제목은 5자 이상 200자 이하여야 합니다.")
    String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다.")
    String content;
}
