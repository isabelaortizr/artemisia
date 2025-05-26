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
                                        // Rutas públicas (sin autenticación)
                                        .requestMatchers("/api/auth/**").permitAll()
                                        .requestMatchers("/swagger-ui/**").permitAll()
                                        .requestMatchers("/v3/api-docs/**").permitAll()

                                        // Rutas de productos
                                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/products").permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/api/products/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").permitAll()

                                        // Rutas de usuarios
                                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/api/users/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").permitAll()

                                        // Rutas de direcciones
                                        .requestMatchers(HttpMethod.GET, "/api/addresses").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/addresses/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/addresses").permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/api/addresses/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/addresses/**").permitAll()

                                        // Rutas de notas de venta
                                        .requestMatchers(HttpMethod.GET, "/api/notas-venta").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/notas-venta/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/notas-venta").permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/api/notas-venta/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/notas-venta/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/notas-venta/**/complete").permitAll()

                                        // Rutas de detalles de orden
                                        .requestMatchers(HttpMethod.GET, "/api/order-details").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/order-details/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/order-details").permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/api/order-details/**").permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/order-details/**").permitAll()

                                        // Rutas de logs (solo administradores)
                                        .requestMatchers("/api/logs/**").permitAll()

                                        // Cualquier otra solicitud requiere autenticación
                                        .anyRequest().permitAll()
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
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}