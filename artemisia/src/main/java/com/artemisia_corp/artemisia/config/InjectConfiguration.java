package com.upb.modulo_01.config;

import com.upb.modulo_01.entity.MyUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@Configuration
public class InjectConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("ADMIN");
            }
            if(authentication.getPrincipal()==null || authentication.getPrincipal() instanceof String) {
                return Optional.of("ADMIN");
            }
            MyUser user = (MyUser) authentication.getPrincipal();
            try {
                return Optional.ofNullable(user.getUsername());
            } catch (Exception e) {
                return Optional.of("ADMIN");
            }
        };
    }

    /**
     * Bean necesario para que jackson serialize y desserialize los tipos de datos Date, usando el timezone de la jvm
     *
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}
