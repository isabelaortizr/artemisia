package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.users.UserDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.users.UserRequestDto;
import com.artemisia_corp.artemisia.entity.dto.users.UserResponseDto;
import com.artemisia_corp.artemisia.entity.dto.users.UserUpdateDto;

import java.util.List;

public interface UsersService {
    List<UserResponseDto> listAll();
    void save(UserRequestDto userRequestDto);
    void delete(UserDeleteDto userDeleteDto);
    void update(UserUpdateDto userUpdateDto);
    UserResponseDto getUserById(Long id);
    boolean existsByEmail(String email);
}
