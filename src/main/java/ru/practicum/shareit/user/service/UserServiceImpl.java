package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToUserId = new HashMap<>();
    private Long nextId = 1L;
    
    @Override
    public UserDto createUser(UserDto userDto) {
        if (emailToUserId.containsKey(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        
        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        emailToUserId.put(user.getEmail(), user.getId());
        return UserMapper.toDto(user);
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
            Long existingUserId = emailToUserId.get(userDto.getEmail());
            if (existingUserId != null && !existingUserId.equals(userId)) {
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }
            
            emailToUserId.remove(existingUser.getEmail());
            existingUser.setEmail(userDto.getEmail());
            emailToUserId.put(userDto.getEmail(), userId);
        }
        
        return UserMapper.toDto(existingUser);
    }
    
    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        return UserMapper.toDto(user);
    }
    
    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toDto(new ArrayList<>(users.values()));
    }
    
    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        User user = users.remove(userId);
        emailToUserId.remove(user.getEmail());
    }
}
