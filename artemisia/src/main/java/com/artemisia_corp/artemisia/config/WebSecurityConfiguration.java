package com.artemisia_corp.artemisia.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.Serializable;
import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration implements WebMvcConfigurer, Serializable {

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http, CorsFilter corsFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(corsFilter, SessionManagementFilter.class)
                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry ->
                                authorizationManagerRequestMatcherRegistry
                                        // Rutas de productos
                                        .requestMatchers("/api/products").permitAll()
                                        .requestMatchers("/api/products/**").permitAll()

                                        // Rutas de usuarios
                                        .requestMatchers("/api/users").permitAll()
                                        .requestMatchers("/api/users/**").permitAll()

                                        // Rutas de direcciones
                                        .requestMatchers("/api/addresses").permitAll()
                                        .requestMatchers("/api/addresses/**").permitAll()

                                        // Rutas de notas de venta
                                        .requestMatchers("/api/notas-venta").permitAll()
                                        .requestMatchers("/api/notas-venta/**").permitAll()

                                        // Rutas de detalles de orden
                                        .requestMatchers("/api/order-details").permitAll()
                                        .requestMatchers("/api/order-details/**").permitAll()

                                        // Rutas de logs (solo administradores)
                                        .requestMatchers("/api/logs").permitAll()
                )
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .cors((cors) -> cors.configurationSource(apiConfigurationSource()));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CorsConfigurationSource apiConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}