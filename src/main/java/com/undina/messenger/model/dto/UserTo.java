package com.undina.messenger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTo {
    private String email;
    private String login;
    private String firstName;
    private String lastName;
}
