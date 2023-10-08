package com.undina.messenger.controller;


import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.*;
import com.undina.messenger.security.JWTToken;
import com.undina.messenger.security.SecurityUser;
import com.undina.messenger.service.UserService;
import com.undina.messenger.validation.ValidationErrorBuilder;
import com.undina.messenger.validation.exceptions.ApplicationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(value = UserController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UserController {

    static final String REST_URL = "/users";
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Login user with email and password to obtain JWT access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created the user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials", content = @Content),
            @ApiResponse(responseCode = "403", description = "Account is blocked, contact support", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(HttpServletRequest request, @Valid @RequestBody LoginRequest loginRequest, Errors errors) {
        log.info("authenticate {}", loginRequest);
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException ex) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
        }
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @Operation(summary = "Logout user ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout the user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials", content = @Content),
            @ApiResponse(responseCode = "403", description = "Account is blocked, contact support", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        log.info("logout");

        session.invalidate();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setValue(null);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(summary = "Sign up new user to work with API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created the user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)})
    @PostMapping("/signup")
    @Validated
    public ResponseEntity<?> registerUser(HttpServletRequest request, @RequestBody @Valid RegisterUser registerUser, Errors errors) {
        log.info("register {}", registerUser);
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.unprocessableEntity().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        return new ResponseEntity<>(userService.signup(registerUser), HttpStatus.CREATED);
    }

    @Operation(summary = "Activate User by Id with code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is activated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "404", description = "Unable to find User", content = @Content),
            @ApiResponse(responseCode = "406", description = "Code in not acceptable", content = @Content),
            @ApiResponse(responseCode = "409", description = "Account already activated", content = @Content)})
    @GetMapping("/activate/{userId}/{verifyCode}")
    public ResponseEntity<?> activateUserWithCode(
            @Parameter(description = "UUID of a User")
            @PathVariable UUID userId,
            @Parameter(description = "Verification code")
            @PathVariable @Size(max = 12) String verifyCode) {
        log.info("Activate User {}", userId);
        return new ResponseEntity<>(userService.activateUserWithCode(userId, verifyCode), HttpStatus.OK);
    }

    @Operation(summary = "Get information about current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserTo.class))}),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)})
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                return ResponseEntity.ok(userService.getUserInfo(user.getId()));
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }


    @PatchMapping("/me")
    public ResponseEntity<?> changeCurrentUserInfo(
            HttpServletRequest request, @Valid @RequestBody UpdateUser updateUser, Errors errors) {
        log.info("Change current user info");
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();

                return ResponseEntity.ok(userService.changeUserInfo(user.getId(), updateUser));
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }

    @Operation(summary = " Change email by Id with code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email is changed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "404", description = "Unable to find User", content = @Content),
            @ApiResponse(responseCode = "406", description = "Code in not acceptable", content = @Content),
    })
    @GetMapping("/email/{newEmail}/{userId}/{verifyCode}")
    public ResponseEntity<?> changeUserEmail(
            @Parameter(description = "New email")
            @PathVariable  String newEmail,
            @Parameter(description = "UUID of a User")
            @PathVariable UUID userId,
            @Parameter(description = "Verification code")
            @PathVariable @Size(max = 12) String verifyCode
    ) {
        log.info("Activate User {}", userId);
        return new ResponseEntity<>(userService.changeUserEmail(newEmail, userId, verifyCode), HttpStatus.OK);
    }

    @Operation(summary = "Change current user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Changed current user password",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid password supplied",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)
    })
    @PatchMapping("/password")
    public ResponseEntity<?> changeCurrentUserPassword(
            HttpServletRequest request, @Valid @RequestBody PasswordDto passwordDto, Errors errors) {
        log.info("Change current user password");
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid password supplied");
                }
                return ResponseEntity.ok(userService.changeUserPassword(user, passwordDto.getNewPassword()));
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }

    @Operation(summary = "Change current user status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Changed current user status",
                    content = @Content
                    ),
            @ApiResponse(responseCode = "400", description = "Invalid password supplied",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)
    })
    @PatchMapping("/blocked/{isBlocked}")
    public ResponseEntity<?> changeCurrentUserStatus(
            @PathVariable("isBlocked") boolean isBlocked) {
        log.info("Change current user status");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                userService.changeUserStatus(user.getId(), isBlocked);
                return ResponseEntity.ok().build();
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }

    @Operation(summary = "Delete current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete current user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTToken.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid password supplied",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCurrentUser(
            HttpServletRequest request,  Errors errors) {
        log.info("Delete current user");
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                userService.deleteUser(user.getId());
                return ResponseEntity.ok().build();
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }
    @Operation(summary = "Add user in friends")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Add user in friends",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserTo.class))}),
            @ApiResponse(responseCode = "400", description = "User friends is closed",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)
    })
    @PostMapping("/friend/{friendLogin}")
    public ResponseEntity<?> addToFriends(
            @PathVariable String friendLogin) {
        log.info("Change current user info");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();

                return ResponseEntity.ok(userService.addUserToFriends(user.getId(), friendLogin));
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }

    @Operation(summary = "Get user friends")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user friends",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserTo.class))}),
            @ApiResponse(responseCode = "400", description = "User friends is closed",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content)
    })
    @GetMapping("/friend/{friendLogin}")
    public ResponseEntity<Set<UserTo>> getFriends(
            @PathVariable String friendLogin) {
        log.info("Change current user info");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();

                return ResponseEntity.ok(userService.getUserFriends(user.getId(), friendLogin));
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }


}


