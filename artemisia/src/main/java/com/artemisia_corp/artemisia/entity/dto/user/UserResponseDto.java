package com.artemisia_corp.artemisia.entity.dto.user;

import com.artemisia_corp.artemisia.entity.User;
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

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.mail = user.getMail();
        this.role = user.getRole().name();
    }
}


