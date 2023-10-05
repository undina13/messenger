package com.undina.messenger.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.undina.messenger.validation.exceptions.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {
    private String secret;
    private Long jwtExpiration;

    public JWTUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") Long jwtExpiration) {
        this.secret = secret;
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String userId, String userRole) {
        Date expirationTime = Date.from(ZonedDateTime.now().plusMinutes(jwtExpiration).toInstant());
        return JWT.create()
                .withClaim("user_id", userId)
                .withClaim("user_role", userRole)
                .withExpiresAt(expirationTime)
                .withClaim("typ", "access")
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndReturnEmail(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withClaim("typ", "access").build();
        DecodedJWT jwt;
        try {
            jwt = verifier.verify(token);
        } catch (TokenExpiredException ex) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Expired JWT Token");
        }
        return jwt.getClaim("user_id").asString();
    }
}
