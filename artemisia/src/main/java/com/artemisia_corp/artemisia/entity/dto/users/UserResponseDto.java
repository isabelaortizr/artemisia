package com.artemisia_corp.artemisia.entity.dto.users;

import com.artemisia_corp.artemisia.entity.Users;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String name;
    private String mail;
    private String role;

    public UserResponseDto(Users user) {
        this.id = user.getId();
        this.name = user.getName();
        this.mail = user.getMail();
        this.role = user.getRole().name();
    }
}


