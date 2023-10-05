package com.undina.messenger.model.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegisterUser {
    @Email(message = "Email should be in right format")
    @NotBlank(message = "Email can't be blank")
    @NotNull(message = "Email can't be null")
    private String email;
    @NotBlank(message = "Password can't be blank")
    @NotNull(message = "Password can't be null")
    @Size(min = 8, max =  30, message = "Password must be between 8 and 30 characters")
    private String password;
    @NotBlank(message = "Login can't be blank")
    @NotNull(message = "Login can't be null")
    private String login;
    @NotBlank(message = "First name can't be blank")
    @NotNull(message = "First name can't be null")
    private String firstName;
    @NotBlank(message = "Last name can't be blank")
    @NotNull(message = "Last name can't be null")
    private String lastName;
}
