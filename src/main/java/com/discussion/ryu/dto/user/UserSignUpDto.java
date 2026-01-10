package com.discussion.ryu.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
public class UserSignUpDto {

    private String username;

    private String password;

    @NotBlank
    private String name;

    @Email
    private String email;
}
