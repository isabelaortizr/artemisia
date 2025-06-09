package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.user.UserResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.user.UserResponseDto(u) " +
            "FROM User u")
    List<UserResponseDto> findAllUsers();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.user.UserResponseDto(u) " +
            "FROM User u WHERE (u.name = :identifier OR u.mail = :identifier) AND u.password = :password")
    UserResponseDto loginUser(@Param("identifier") String identifier, @Param("password") String password);

    Optional<User> findByMail(String mail);
    Optional<User> findByName(String name);
    boolean existsByMail(String mail);
}
