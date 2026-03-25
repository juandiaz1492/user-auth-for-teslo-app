package com.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.user.dto.LoginDto;
import com.user.dto.UserDto;
import com.user.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    User dtotoUser (UserDto dto); 

    UserDto UserToDtoUser (User user); 

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "username", ignore = true)
    User loginDtoToUser (LoginDto dto); 


    LoginDto userToLoginDto (User user);  
}

