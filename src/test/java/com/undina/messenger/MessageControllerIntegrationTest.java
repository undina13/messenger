package com.undina.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.CreateMessageDto;
import com.undina.messenger.model.dto.FullMessageDto;
import com.undina.messenger.model.dto.UserTo;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc()
@Sql("/message_data.sql")
public class MessageControllerIntegrationTest {
//    public static final User USER_1 = new User("b4861725-8aa5-4770-8e78-c9b6694dc975", "user1@mail.ru",
//            "$2a$10$lTgowhKpte2llERILz/C9ermIl9Q.ICoDa0ZkkLSm9dR2OeNdtKuW", "ROLE_USER");
//    public static final User USER_2 = new User("f09108bf-b0ff-4017-85e9-8799b702f246", "user2@mail.ru",
//            "$2a$10$lTgowhKpte2llERILz/C9ermIl9Q.ICoDa0ZkkLSm9dR2OeNdtKuW", "ROLE_USER");
//
//    public static final UserTo USER_1_To = new UserTo("user1@mail.ru", "Ivan",
//            "Ivan", "Ivanov");
//    public static final UserTo USER_2_To = new UserTo("user2@mail.ru", "Petr",
//            "Petr", "Petrov");



    public static final CreateMessageDto CREATE_MESSAGE_DTO = new CreateMessageDto(
          "Petr", "some text");

    public static final CreateMessageDto CREATE_MESSAGE_DTO1 = new CreateMessageDto(
            "Sidor", "some text");
    public static final CreateMessageDto CREATE_WRONG_MESSAGE_DTO = new CreateMessageDto(
            "Petrrr", "some text");

    public static final FullMessageDto FULL_MESSAGE_DTO_2 = new FullMessageDto(
            "b4861725-8aa5-4770-8e78-c9b6694dc991", "Petr", "Ivan", "text1answer2", LocalDateTime
            .of(2023, 10, 2, 19, 14, 10));
    public static final FullMessageDto FULL_MESSAGE_DTO_3 = new FullMessageDto("b4861725-8aa5-4770-8e78-c9b6694dc992",
             "Ivan", "Petr", "text3", LocalDateTime
            .of(2023, 10, 3, 19, 13, 10));
    public static final FullMessageDto FULL_MESSAGE_DTO_4 = new FullMessageDto("b4861725-8aa5-4770-8e78-c9b6694dc993",
             "Petr", "Ivan", "text3answer4", LocalDateTime
            .of(2023, 10, 3, 19, 14, 10));

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    protected ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mockMvc.perform(builder);
    }

    @Test
    @DirtiesContext
    @WithUserDetails(value = "user1@mail.ru")
    void testCreateMessage() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_MESSAGE_DTO))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.text").value(CREATE_MESSAGE_DTO.getText()));
    }

    @Test
    @DirtiesContext
    @WithUserDetails(value = "user1@mail.ru")
    void testCreateMessageClosedFriends() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_MESSAGE_DTO1))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    @WithUserDetails(value = "user2@mail.ru")
    void testCreateMessageClosedFriendsByFriend() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_MESSAGE_DTO1))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.text").value(CREATE_MESSAGE_DTO1.getText()));
    }

    @Test
    @WithUserDetails(value = "user2@mail.ru")
    void testCreateMessageWrongUser() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_MESSAGE_DTO))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void testCreateWrongMessage() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_WRONG_MESSAGE_DTO))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMessageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post("/messages")
                .content(mapper.writeValueAsString(CREATE_MESSAGE_DTO))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "user2@mail.ru")
    void testGetMessagesByUser() throws Exception {
        perform(MockMvcRequestBuilders.get("/messages/chat/Ivan" ))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(FULL_MESSAGE_DTO_4, FULL_MESSAGE_DTO_3, FULL_MESSAGE_DTO_2))));
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void testGetMessagesByUserOk() throws Exception {
        perform(MockMvcRequestBuilders.get("/messages/chat/Petr" ))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(FULL_MESSAGE_DTO_4, FULL_MESSAGE_DTO_3, FULL_MESSAGE_DTO_2))));
    }

    @Test
    @WithUserDetails(value = "user1@mail.ru")
    void testGetMessagesByUserEmpty() throws Exception {
        perform(MockMvcRequestBuilders.get("/messages/chat/Sidor" ))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(new ArrayList<>())));
    }


}
