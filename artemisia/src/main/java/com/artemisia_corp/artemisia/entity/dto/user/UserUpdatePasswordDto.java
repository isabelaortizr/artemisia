package com.artemisia_corp.artemisia.entity.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class UserUpdatePasswordDto {
    private Long userId;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    public UserUpdatePasswordDto(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        this.userId = userId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }
}
