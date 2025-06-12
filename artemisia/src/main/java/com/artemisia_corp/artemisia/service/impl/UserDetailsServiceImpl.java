package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}", username);
        User authUser = this.userRepository.findByName(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));
        String password = authUser.getPassword();
        return new org.springframework.security.core.userdetails.User(
                authUser.getUsername(),
                password,
                authUser.isEnabled(),
                true,
                authUser.isAccountNonExpired(),
                true,
                authUser.getAuthorities()
        );
    }

}
