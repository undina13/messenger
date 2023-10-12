package com.undina.messenger.controller;

import com.undina.messenger.AbstractTest;
import com.undina.messenger.model.dto.CreateMessageDto;
import com.undina.messenger.model.dto.FullMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql("/message_data.sql")
public class MessageControllerIntegrationTest extends AbstractTest {

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
                .andExpect(status().isUnauthorized());
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
