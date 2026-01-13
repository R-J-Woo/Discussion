package com.discussion.ryu.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserPasswordDto {
    @NotBlank
    String currentPassword;
    @NotBlank
    String newPassword;
    @NotBlank
    String newPasswordConfirm;
}
