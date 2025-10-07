package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
    
    public static User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
    
    public static List<UserDto> toDto(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }
}
