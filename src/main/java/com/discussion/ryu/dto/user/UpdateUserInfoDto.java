package com.discussion.ryu.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserInfoDto {

    @NotBlank
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    String name;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank
    String email;
}
