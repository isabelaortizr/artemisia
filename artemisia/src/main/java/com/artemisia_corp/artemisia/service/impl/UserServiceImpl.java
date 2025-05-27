package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.user.*;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LogsService logsService;

    @Override
    public List<UserResponseDto> getAllUsers() {
        logsService.info("Fetching all users");
        return userRepository.findAllUsers();
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        logsService.info("Fetching user with ID: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    throw new RuntimeException("User not found");
                });
        return convertToDto(user);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userDto) {
        if (userRepository.existsByMail(userDto.getMail())) {
            logsService.error("Email already in use: " + userDto.getMail());
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(userDto.getName())
                .mail(userDto.getMail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.valueOf(userDto.getRole()))
                .build();

        User savedUser = userRepository.save(user);
        logsService.info("User created with ID: " + savedUser.getId());
        return convertToDto(savedUser);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    throw new RuntimeException("User not found");
                });

        if (!user.getMail().equals(userDto.getMail())) {
            if (userRepository.existsByMail(userDto.getMail())) {
                logsService.error("Email already in use: " + userDto.getMail());
                throw new RuntimeException("Email already in use");
            }
        }

        user.setName(userDto.getName());
        user.setMail(userDto.getMail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        if (userDto.getRole() != null) {
            user.setRole(UserRole.valueOf(userDto.getRole()));
        }

        User updatedUser = userRepository.save(user);
        logsService.info("User updated with ID: " + updatedUser.getId());
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            logsService.error("User not found with ID: " + id);
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
        logsService.info("User deleted with ID: " + id);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        logsService.info("Fetching user by email: " + email);
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> {
                    logsService.error("User not found with email: " + email);
                    throw new RuntimeException("User not found");
                });
        return convertToDto(user);
    }

    private UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .mail(user.getMail())
                .role(user.getRole().name())
                .build();
    }
}