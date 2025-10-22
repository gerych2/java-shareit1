package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        
        User user = UserMapper.toUser(userDto);
        user = userRepository.save(user);
        return UserMapper.toDto(user);
    }
    
    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            Optional<User> userWithEmail = userRepository.findByEmail(userDto.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }
            existingUser.setEmail(userDto.getEmail());
        }
        
        existingUser = userRepository.save(existingUser);
        return UserMapper.toDto(existingUser);
    }
    
    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toDto(user);
    }
    
    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toDto(userRepository.findAll());
    }
    
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
