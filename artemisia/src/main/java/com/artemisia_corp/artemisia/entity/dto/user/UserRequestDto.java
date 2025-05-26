package com.artemisia_corp.artemisia.entity.dto.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserRequestDto {
    private String name;
    private String mail;
    private String password;
    private String role;
}
