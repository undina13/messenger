package com.undina.messenger.controller;

import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.CreateMessageDto;
import com.undina.messenger.model.dto.FullMessageDto;
import com.undina.messenger.security.SecurityUser;
import com.undina.messenger.service.MessageService;
import com.undina.messenger.service.UserService;
import com.undina.messenger.validation.ValidationErrorBuilder;
import com.undina.messenger.validation.exceptions.ApplicationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping(value = MessageController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class MessageController {
    static final String REST_URL = "/messages";
    private final UserService userService;
    private final MessageService messageService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Create message from Current User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message is created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateMessageDto.class))}),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Account is blocked, contact support",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Unable to save message",
                    content = @Content)})
    @PostMapping
    public ResponseEntity<?> createMessage(
            HttpServletRequest request,
            @RequestBody @Valid CreateMessageDto createMessageDto,
            Errors errors) {
        if (errors.hasErrors()) {
            log.info("Validation error with request: " + request.getRequestURI());
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                return new ResponseEntity<>(
                        messageService.createMessage(createMessageDto, user.getId()),
                        HttpStatus.CREATED
                );
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }

    @Operation(summary = "Get list of messages for the recipient by User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FullMessageDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Wrong credentials",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only  owner can get messages",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Unable to find user with such login",
                    content = @Content)})
    @GetMapping("/chat/{recipientLogin}")
    public ResponseEntity<?> getMessagesForContractByUser(
            @PathVariable String recipientLogin) {
        log.info("Get messages for user login {}", recipientLogin);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser) {
                User user = ((SecurityUser) principal).getUser();
                return ResponseEntity.ok(
                        messageService.getMessagesForUserLogin(recipientLogin, user.getId())
                );
            }
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Wrong credentials");
    }
}
