package com.undina.messenger.mapper;


import com.undina.messenger.model.Message;
import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.CreateMessageDto;
import com.undina.messenger.model.dto.FullMessageDto;
import com.undina.messenger.model.dto.RegisterUser;
import com.undina.messenger.model.dto.UserTo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MessageMapper {
    Message toMessage(CreateMessageDto createMessageDto);

    @Mapping(target = "authorLogin", source = "author.login")
    @Mapping(target = "recipientLogin", source = "recipient.login")
    FullMessageDto toFullMessageDto(Message message);

}


