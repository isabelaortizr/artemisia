package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.security.AuthenticationDto;
import com.artemisia_corp.artemisia.entity.dto.security.OKAuthDto;
import com.artemisia_corp.artemisia.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/token")
    public ResponseEntity<OKAuthDto> token(@RequestBody AuthenticationDto data) {
        String username = data.getUsername();
        User user;
        try {
            Optional<User> userOptional = userService.getUserByName(data.getUsername());
            if (userOptional.isEmpty()) {
                throw new BadCredentialsException("Email o contraseña son incorrectos");
            }
            user = userOptional.get();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(data.getUsername(), data.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities()));

            String token = jwtTokenProvider.createToken(user);
            OKAuthDto  okAuthDto = new OKAuthDto();
            okAuthDto.setIdToken(token);
            okAuthDto.setUsername(user.getUsername());
            return ok(okAuthDto);
        } catch (BadCredentialsException e) {
            log.error("Error al autentificar el usuario: {}", data.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        } catch (
                Exception e) {
            log.error("Error al autentificar el usuario: {}", data.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autentificar el usuario: " + data.getUsername());
        }
    }

}
