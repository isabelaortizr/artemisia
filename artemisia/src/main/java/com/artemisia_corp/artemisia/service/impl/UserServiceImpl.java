package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.user.*;
import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LogsService logsService;
    @Autowired
    @Lazy
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public List<UserResponseDto> getAllUsers() {
        logsService.info("Fetching all users");
        return userRepository.findAllUsers();
    }

    @Override
    public UserResponseDto login(LoginDto loginDto) {
        logsService.info("Logging in user for: " + loginDto.getName());
        if (((loginDto.getName() != null && !loginDto.getName().isEmpty())
                || (loginDto.getMail() != null && !loginDto.getMail().isEmpty()))
                && (loginDto.getPassword() != null && !loginDto.getPassword().isEmpty())) {
            logsService.info("Encripting passowrd");
            String encriptedPassword = passwordEncoder.encode(loginDto.getPassword());


            if (!loginDto.getName().isEmpty()) return userRepository.loginUser(loginDto.getName(), encriptedPassword);
            else if (!loginDto.getMail().isEmpty()) return userRepository.loginUser(loginDto.getMail(), encriptedPassword);
            else throw new RuntimeException("Invalid login credentials");
        }

        return null;
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
    public Optional<User> findByUserIdToValidateSession(Long id) {
        return this.userRepository.findById(id);
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
                .status(StateEntity.valueOf("ACTIVE"))
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
    public void deleteUser(Long id, String token) {
        String userNameFromToken = jwtTokenProvider.getUserIdFromToken(token);
        Optional<User> userOptional = userRepository.findByName(userNameFromToken);
        if (userOptional.isEmpty()) {
            logsService.error("User not found with name: " + userNameFromToken);
            throw new RuntimeException("User not found");
        }
        User user = userOptional.get();

        if (!user.getId().equals(id)) {
            logsService.error("Unauthorized delete attempt by user ID: " + userNameFromToken);
            throw new RuntimeException("Unauthorized delete attempt");
        }

        if (!userRepository.existsById(id)) {
            logsService.error("User not found with ID: " + id);
            throw new RuntimeException("User not found");
        }

        user.setStatus(StateEntity.valueOf("DELETED"));
        user.setName(user.getName() + " DELETED");
        user.setMail(user.getMail() + " DELETED");
        userRepository.save(user);
        logsService.info("User deleted with ID: " + id);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        logsService.info("Fetching user by email: " + email);
        User user = userRepository.findUserByMail(email)
                .orElseThrow(() -> {
                    logsService.error("User not found with email: " + email);
                    throw new RuntimeException("User not found");
                });
        return convertToDto(user);
    }

    @Override
    public Optional<User> getUserByName(String name) {
        logsService.info("Fetching user by name: " + name);
        return userRepository.findByName(name);
    }

    @Override
    public UserResponseDto updateEmail(UserUpdateEmailDto emailDto) {
        Long userId = emailDto.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + userId);
                    throw new RuntimeException("User not found");
                });

        if (!passwordEncoder.matches(emailDto.getCurrentPassword(), user.getPassword())) {
            logsService.error("Invalid current password for user ID: " + userId);
            throw new RuntimeException("Invalid current password");
        }

        if (userRepository.existsByMail(emailDto.getNewEmail())) {
            logsService.error("Email already in use: " + emailDto.getNewEmail());
            throw new RuntimeException("Email already in use");
        }

        user.setMail(emailDto.getNewEmail());
        User updatedUser = userRepository.save(user);
        logsService.info("Email updated for user ID: " + userId);
        return convertToDto(updatedUser);
    }

    @Override
    public UserResponseDto updatePassword(UserUpdatePasswordDto passwordDto) {
        Long userId = passwordDto.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + userId);
                    throw new RuntimeException("User not found");
                });

        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            logsService.error("Invalid current password for user ID: " + userId);
            throw new RuntimeException("Invalid current password");
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword())) {
            logsService.error("New passwords don't match for user ID: " + userId);
            throw new RuntimeException("New passwords don't match");
        }

        if (passwordEncoder.matches(passwordDto.getNewPassword(), user.getPassword())) {
            logsService.error("New password must be different from current for user ID: " + userId);
            throw new RuntimeException("New password must be different from current");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        User updatedUser = userRepository.save(user);
        logsService.info("Password updated for user ID: " + userId);
        return convertToDto(updatedUser);
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