package com.undina.messenger.mapper;


import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.RegisterUser;
import com.undina.messenger.model.dto.UserTo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {
    User toUser(RegisterUser registerUser);

    UserTo toUserTo(User user);
}


