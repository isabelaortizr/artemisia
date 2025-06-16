package com.artemisia_corp.artemisia.entity.dto.security;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDto {
    private String username;
    private String password;
}
