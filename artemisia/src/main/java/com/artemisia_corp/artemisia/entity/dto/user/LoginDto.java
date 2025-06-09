package com.artemisia_corp.artemisia.entity.dto.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginDto {
    private String name;
    private String mail;
    private String password;
}
