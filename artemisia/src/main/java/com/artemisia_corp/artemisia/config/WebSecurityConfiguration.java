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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.Serializable;

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
                                        .requestMatchers("/api/v1/companies").permitAll()
                                        .requestMatchers("/api/v1/companies/save").permitAll()
                                        .requestMatchers("/api/v1/companies/delete").permitAll()
                                        .requestMatchers("/api/v1/companies/update").permitAll()
                                        .requestMatchers("/api/v1/products").permitAll()
                                        .requestMatchers("/api/v1/products/get_name").permitAll()
                                        .requestMatchers("/api/v1/products/save").permitAll()
                                        .requestMatchers("/api/v1/products/delete").permitAll()
                                        .requestMatchers("/api/v1/products/update").permitAll()
                                        .requestMatchers("/api/v1/workers").permitAll()
                                        .requestMatchers("/api/v1/workers/save").permitAll()
                                        .requestMatchers("/api/v1/workers/delete").permitAll()
                                        .requestMatchers("/api/v1/workers/update").permitAll()
                                        .requestMatchers("/api/v1/clients").permitAll()
                                        .requestMatchers("/api/v1/clients/save").permitAll()
                                        .requestMatchers("/api/v1/clients/delete").permitAll()
                                        .requestMatchers("/api/v1/clients/update").permitAll()
                                        .requestMatchers("/api/v1/alumni").permitAll()
                                        .requestMatchers("/api/v1/alumni/save").permitAll()
                                        .anyRequest().authenticated()

                )
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .cors((cors) -> cors.configurationSource(apiConfigurationSource()));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private CorsConfigurationSource apiConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



}
