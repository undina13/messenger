package com.undina.messenger.model.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PasswordDto {
    @NotBlank(message = "Old Password can't be blank")
    @NotNull(message = "Old Password can't be null")
    @Size(min = 8, max =  30, message = "Old Password must be between 8 and 30 characters")
    private String oldPassword;
    @NotBlank(message = "Password can't be blank")
    @NotNull(message = "Password can't be null")
    @Size(min = 8, max =  30, message = "Password must be between 8 and 30 characters")
    private String newPassword;
}
