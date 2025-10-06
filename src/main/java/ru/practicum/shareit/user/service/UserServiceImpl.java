package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToUserId = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        validateUser(userDto);

        if (emailToUserId.containsKey(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        emailToUserId.put(user.getEmail(), user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (!isValidEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("Некорректный email");
            }

            Long existingUserId = emailToUserId.get(userDto.getEmail());
            if (existingUserId != null && !existingUserId.equals(userId)) {
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }

            emailToUserId.remove(existingUser.getEmail());
            existingUser.setEmail(userDto.getEmail());
            emailToUserId.put(userDto.getEmail(), userId);
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        User user = users.remove(userId);
        emailToUserId.remove(user.getEmail());
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!isValidEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Некорректный email");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
