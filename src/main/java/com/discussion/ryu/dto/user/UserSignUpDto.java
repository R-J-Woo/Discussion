package com.discussion.ryu.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpDto {

    @NotBlank
    @Size(min = 8, max = 20, message = "아이디는 8자 이상 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    private String name;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank
    private String email;
}
