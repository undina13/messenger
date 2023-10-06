package com.undina.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc()
@Sql("/user_data.sql")
public class UserControllerIntegrationTest {
    public static final User USER_1 = new User("b4861725-8aa5-4770-8e78-c9b6694dc975", "user1@mail.ru",
            "$2a$10$lTgowhKpte2llERILz/C9ermIl9Q.ICoDa0ZkkLSm9dR2OeNdtKuW", "ROLE_USER");

    public static final UserTo USER_1_To = new UserTo("user1@mail.ru", "Ivan",
            "Ivan", "Ivanov");
    public static final UserTo USER_2_To = new UserTo("user1@mail.ru", "superLogin",
            "Ivan", "Ivanov");


    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;


    protected ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mockMvc.perform(builder);
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void testGetMe() throws Exception {
        perform(get("/users/me"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(USER_1_To)));
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void testLoginMe() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(USER_1.getEmail());
        request.setPassword("password");
        perform(post("/users/login")
                .content(mapper.writeValueAsString(request))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    void signupUser() throws Exception {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setEmail("user22@mail.ru");
        registerUser.setPassword("password");
        registerUser.setFirstName("Alex");
        registerUser.setLastName("Petrov");
        registerUser.setLogin("Logg");
        perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerUser)))
                .andExpect(status().isCreated());
    }

    @Test
    void signupUserInvalidNullEmail() throws Exception {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setEmail(null);
        registerUser.setPassword("password");
        registerUser.setFirstName("Alex");
        registerUser.setLastName("Petrov");
        registerUser.setLogin("Logg");
        perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupUserInvalidBlankEmail() throws Exception {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setEmail("");
        registerUser.setPassword("password");
        registerUser.setFirstName("Alex");
        registerUser.setLastName("Petrov");
        registerUser.setLogin("Logg");
        perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupUserInvalidNullPassword() throws Exception {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setEmail("user22@mail.ru");
        registerUser.setFirstName("Alex");
        registerUser.setLastName("Petrov");
        registerUser.setLogin("Logg");
        perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnAuth() throws Exception {
        perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void changeUserPassword() throws Exception {
        PasswordDto passwordDto = new PasswordDto();
        passwordDto.setOldPassword("password");
        passwordDto.setNewPassword("new_password");
        perform(patch("/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(passwordDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    @WithUserDetails(value = "user1@mail.ru")
    void changeUserInfo() throws Exception {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setLogin("superLogin");
        perform(patch("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());

        perform(get("/users/me"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(USER_2_To)));
    }
}
