package com.upb.modulo_01.controller;

import com.upb.modulo_01.entity.MyUser;
import com.upb.modulo_01.entity.dto.AuthenticationDto;
import com.upb.modulo_01.entity.dto.OKAuthDto;
import com.upb.modulo_01.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final com.upb.modulo_01.config.JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/token")
    public ResponseEntity<OKAuthDto> token(@RequestBody AuthenticationDto data) {
        String username = data.getUsername();
        MyUser user;
        try {
            Optional<MyUser> userOptional = userService.findByUsername(data.getUsername());
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        } catch (
                Exception e) {
            log.error("Error al autentificar el usuario: {}", data.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autentificar el usuario: " + data.getUsername());
        }
    }

}
