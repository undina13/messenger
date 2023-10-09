package com.undina.messenger.model.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UpdateUser {
    private String email;
    private String login;
    private String firstName;
    private String lastName;
    private Boolean isClosedMessages;
    private Boolean isClosedFriends;
}
