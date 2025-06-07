package com.upb.modulo_01.config;

import com.upb.modulo_01.entity.MyUser;
import com.upb.modulo_01.exception.InvalidJwtAuthenticationException;
import com.upb.modulo_01.exception.NotDataFoundException;
import com.upb.modulo_01.service.UserService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider implements Serializable {
    private static final String USER_ID_CLAIM = "user_id";

    @Value("${security.jwt.token.secret-key:Ch4ng1t}")
    private String secretKey;
    private byte[] secretKeyByte;
    @Value("${security.jwt.token.expire-length:28800000}")
    private int validityInMinutes;
    @Autowired
    private UserService userService;


    @PostConstruct
    protected void init() {
        secretKeyByte = Base64.getDecoder().decode(secretKey);
    }

    public String createToken(MyUser user) {
        Date now = new Date();
        Date validity = plusMinutes(now, this.validityInMinutes);
        Claims claims = Jwts.claims()
                .setSubject(user.getUsername())
                .setId(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(validity);
        claims.put(USER_ID_CLAIM, user.getId());

        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKeyByte)
                .compact();

        return token;
    }

    private String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    private Long getId(String token) {
        String id= Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getId();
        return Long.parseLong(id);
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            try {
                return bearerToken.substring(7);
            } catch (NotDataFoundException e) {
                log.error("Usuario no encontrado");
                return null;
            }
        }
        return null;
    }

    public Optional<Authentication> validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().after(new Date())) {
                MyUser authUser = this.userService.findByUserIdToValidateSession(getId(token)).orElseThrow(() -> new UsernameNotFoundException("Autenticación incorrecta"));
                return Optional.of(new UsernamePasswordAuthenticationToken(authUser, "", authUser.getAuthorities()));
            }
            return Optional.empty();
        } catch (ExpiredJwtException e) {
            log.warn("La sesión del usuario a expirado");
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error al validar el TOKEN", e);
            throw new InvalidJwtAuthenticationException("Token JWT caducado o no válido.");
        }
    }


    public  Date plusMinutes(Date date, int minutesToAdd) {
        Calendar calDateStart = Calendar.getInstance();
        calDateStart.setTime(date);
        calDateStart.add(Calendar.MINUTE, minutesToAdd);

        return calDateStart.getTime();
    }



}

