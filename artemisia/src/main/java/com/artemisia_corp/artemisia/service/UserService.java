package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.user.UserRequestDto;
import com.artemisia_corp.artemisia.entity.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    UserResponseDto createUser(UserRequestDto userDto);
    UserResponseDto updateUser(Long id, UserRequestDto userDto);
    void deleteUser(Long id);
    UserResponseDto getUserByEmail(String email);
}