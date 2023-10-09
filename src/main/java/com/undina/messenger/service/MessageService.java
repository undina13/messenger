package com.undina.messenger.service;

import com.undina.messenger.mapper.MessageMapper;
import com.undina.messenger.model.Message;
import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.CreateMessageDto;
import com.undina.messenger.model.dto.FullMessageDto;
import com.undina.messenger.repository.MessageRepository;
import com.undina.messenger.repository.UserRepository;
import com.undina.messenger.validation.exceptions.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    @Transactional(readOnly = true)
    public FullMessageDto createMessage(CreateMessageDto createMessageDto, String userId) {
        Message message = messageMapper.toMessage(createMessageDto);
        User author = userRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        if(author.getLogin().equals(createMessageDto.getLogin())){
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "user can not send message himself");
        }
        message.setAuthor(author);
        User recipient = userRepository.findByLogin(createMessageDto.getLogin()).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        message.setRecipient(recipient);
        if(recipient.isClosedMessages()&&!recipient.getFriends().contains(author)){
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Only friends can write this user");
        }
        return messageMapper.toFullMessageDto(messageRepository.save(message));
    }

    public List<FullMessageDto> getMessagesForUserLogin(String recipientLogin, String userId) {
        User author = userRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        User recipient = userRepository.findByLogin(recipientLogin).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        return messageRepository.findAllByAuthorInAndRecipientInOrderByCreatedDesc(List.of(author, recipient), List.of(author, recipient))
                .stream()
                .map(messageMapper::toFullMessageDto)
                .collect(Collectors.toList());
    }
}
