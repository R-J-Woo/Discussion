package com.discussion.ryu.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserInfoDto {

    @NotBlank
    String name;

    @NotBlank
    String email;
}
