package com.undina.messenger.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTToken {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("user_role")
    private String userRole;
    @JsonProperty("access_token")
    private String accessToken;
}
