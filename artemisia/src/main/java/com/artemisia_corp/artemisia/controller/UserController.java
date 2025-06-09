package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.user.*;
import com.artemisia_corp.artemisia.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userDto) {
        UserResponseDto response = userService.createUser(userDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id, @RequestBody UserRequestDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id, @RequestHeader("Authorization") String token) {
        userService.deleteUser(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/email")
    public ResponseEntity<UserResponseDto> updateEmail(
            @RequestBody UserUpdateEmailDto emailDto) {
        return ResponseEntity.ok(userService.updateEmail(emailDto));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updatePassword(
            @RequestBody UserUpdatePasswordDto passwordDto) {
        return ResponseEntity.ok(userService.updatePassword(passwordDto));
    }
}
