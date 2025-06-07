package com.upb.modulo_01.entity.dto;

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
