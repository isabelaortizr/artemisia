package com.artemisia_corp.artemisia.entity.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateEmailDto {
    private Long userId;
    private String newEmail;
    private String currentPassword;
}
