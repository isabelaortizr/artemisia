package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.user.*;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto login(LoginDto LoginDto);
    UserResponseDto getUserById(Long id);
    Optional<User> findByUserIdToValidateSession(Long id);
    UserResponseDto createUser(UserRequestDto userDto);
    UserResponseDto updateUser(Long id, UserRequestDto userDto);
    void deleteUser(Long id, String token);
    UserResponseDto getUserByEmail(String email);
    Optional<User> getUserByName(String name);
    UserResponseDto updateEmail(UserUpdateEmailDto emailDto);
    UserResponseDto updatePassword(UserUpdatePasswordDto passwordDto);
}