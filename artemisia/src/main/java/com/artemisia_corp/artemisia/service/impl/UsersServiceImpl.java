package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Users;
import com.artemisia_corp.artemisia.entity.dto.users.*;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.UsersRepository;
import com.artemisia_corp.artemisia.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> listAll() {
        return usersRepository.findAllUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return usersRepository.findUserById(id);
    }

    @Override
    @Transactional
    public void save(UserRequestDto userDto) {
        if (usersRepository.existsByMail(userDto.getMail())) {
            log.error("Mail already exists");
            throw new RuntimeException("Usuario con correo ya existe");
        }

        Users user = Users.builder()
                .name(userDto.getName())
                .mail(userDto.getMail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.valueOf(userDto.getRole().toUpperCase()))
                .build();

        usersRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(UserDeleteDto userDto) {
        if (!usersRepository.existsById(userDto.getId())) {
            return;
        }
        usersRepository.deleteById(userDto.getId());
    }

    @Override
    @Transactional
    public void update(UserUpdateDto userDto)  {
        Users user = usersRepository.findById(userDto.getId()).orElse(null);
        if (user != null) return;

        // Validar email único si cambia
        if (userDto.getMail() != null && !userDto.getMail().equals(user.getMail())) {
            if (usersRepository.existsByMail(userDto.getMail())) return;
            user.setMail(userDto.getMail());
        }

        // Actualizar campos
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty())
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if (userDto.getRole() != null) user.setRole(UserRole.valueOf(userDto.getRole().toUpperCase()));


        usersRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usersRepository.existsByMail(email);
    }
}