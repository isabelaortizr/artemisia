package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Users;
import com.artemisia_corp.artemisia.entity.dto.users.UserResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.users.UserResponseDto(u) " +
            "FROM Users u")
    List<UserResponseDto> findAllUsers();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.users.UserResponseDto(u) " +
            "FROM Users u WHERE u.id = :id")
    UserResponseDto findUserById(Long id);

    boolean existsByMail(String email);
}
