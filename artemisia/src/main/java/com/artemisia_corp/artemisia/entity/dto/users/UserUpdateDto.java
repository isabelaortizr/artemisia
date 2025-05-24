package com.artemisia_corp.artemisia.entity.dto.users;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserUpdateDto {
    private Long id;
    private String name;
    private String mail;
    private String password;
    private String role;
}
