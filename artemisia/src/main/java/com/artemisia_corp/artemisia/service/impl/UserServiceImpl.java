package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.user.*;
import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogsService logsService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.artemisia_corp.artemisia.service.impl.clients.RecommenderPythonClient recommenderPythonClient;

    @Override
    public List<UserResponseDto> getAllUsers() {
        logsService.info("Fetching all users");
        return userRepository.findAllUsers();
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    return new NotDataFoundException("User not found with ID: " + id);
                });

        return convertToDto(user);
    }

    @Override
    public Optional<User> findByUserIdToValidateSession(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userDto) {
        if (userDto == null || userDto.getMail() == null || userDto.getMail().trim().isEmpty()) {
            logsService.error("User mail is required.");
            throw new IllegalArgumentException("Email is required.");
        }
        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            logsService.error("User name is required.");
            throw new IllegalArgumentException("Username is required.");
        }
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            logsService.error("User password is required.");
            throw new IllegalArgumentException("Password is required.");
        }
        if (userDto.getRole() == null) {
            logsService.error("User role is required.");
            throw new IllegalArgumentException("Role is required.");
        }

        if (userRepository.existsByMail(userDto.getMail())) {
            logsService.error("Email is already in use: " + userDto.getMail());
            throw new IllegalArgumentException("Email is already in use.");
        }

        if (userRepository.existsByName(userDto.getName())) {
            logsService.error("Username is already in use: " + userDto.getName());
            throw new IllegalArgumentException("Username is already in use.");
        }

        User user = User.builder()
                .name(userDto.getName())
                .mail(userDto.getMail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.valueOf(userDto.getRole().trim().toUpperCase()))
                .status(StateEntity.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        try {
            if (recommenderPythonClient != null) {
                boolean ok = recommenderPythonClient.registerUser(savedUser.getId().intValue());
                if (!ok) logsService.warning("Could not register new user in recommender: " + savedUser.getId());
                else logsService.info("Registered new user in recommender: " + savedUser.getId());
            }
        } catch (Exception e) {
            logsService.error("Error registering user in recommender: " + e.getMessage());
        }

        return convertToDto(savedUser);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        if (userDto == null) {
            throw new IllegalArgumentException("User data is required.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("User not found with ID: " + id));

        if (userDto.getMail() != null && !userDto.getMail().trim().isEmpty()) {
            if (!userDto.getMail().equals(user.getMail()) && userRepository.existsByMail(userDto.getMail())) {
                throw new IllegalArgumentException("Email is already in use.");
            }
            user.setMail(userDto.getMail());
        }

        if (userDto.getName() != null && !userDto.getName().trim().isEmpty()) {
            user.setName(userDto.getName());
        }

        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id, String token) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization token is required.");
        }

        User user = userRepository.findUserById(id);

        user.setStatus(StateEntity.DELETED);
        user.setName(user.getName() + " DELETED");
        user.setMail(user.getMail() + " DELETED");
        userRepository.save(user);
        logsService.info("User deleted successfully with ID: " + id);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        User user = userRepository.findUserByMail(email)
                .orElseThrow(() -> new NotDataFoundException("User not found with email: " + email));

        return convertToDto(user);
    }

    @Override
    public Optional<User> getUserByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logsService.error("User name is required.");
            throw new IllegalArgumentException("Username is required.");
        }
        logsService.info("Fetching user by name: " + name);
        return userRepository.findByName(name);
    }

    @Override
    public UserResponseDto updateEmail(UserUpdateEmailDto emailDto) {
        if (emailDto == null || emailDto.getUserId() == null || emailDto.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid user ID is required.");
        }
        if (emailDto.getNewEmail() == null || emailDto.getNewEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("New email is required.");
        }

        User user = userRepository.findById(emailDto.getUserId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + emailDto.getUserId());
                    return new NotDataFoundException("User not found with ID: " + emailDto.getUserId());
                });

        if (!passwordEncoder.matches(emailDto.getCurrentPassword(), user.getPassword())) {
            logsService.error("Invalid current password for user ID: " + emailDto.getUserId());
            throw new NotDataFoundException("Invalid current password");
        }

        if (userRepository.existsByMail(emailDto.getNewEmail())) {
            logsService.error("Email is already in use: " + emailDto.getNewEmail());
            throw new IllegalArgumentException("Email is already in use.");
        }

        user.setMail(emailDto.getNewEmail());
        User updatedUser = userRepository.save(user);
        logsService.info("Email updated for user ID: " + emailDto.getUserId() + " to: " + emailDto.getNewEmail());
        return convertToDto(updatedUser);
    }

    @Override
    public UserResponseDto updatePassword(UserUpdatePasswordDto passwordDto) {
        if (passwordDto == null || passwordDto.getUserId() == null || passwordDto.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid user ID is required.");
        }
        if (passwordDto.getCurrentPassword() == null || passwordDto.getCurrentPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if ((passwordDto.getNewPassword() == null || passwordDto.getNewPassword().trim().isEmpty()) &&
                !Objects.equals(passwordDto.getConfirmNewPassword(), passwordDto.getNewPassword())) {
            throw new IllegalArgumentException("New password is required.");
        }

        User user = userRepository.findById(passwordDto.getUserId())
                .orElseThrow(() -> new NotDataFoundException("User not found with ID: " + passwordDto.getUserId()));

        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        User updatedUser = userRepository.save(user);
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