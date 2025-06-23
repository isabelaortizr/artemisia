package com.artemisia_corp.artemisia.integracion.impl.dtos.StereumUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StereumUserInfo {
    private String id;
    private String status;
    private String user_type;
    private String created_at;
    private String updated_at;
    private String aud;
    private String role;
    private String email;
    private String username;
    private String name;
    private String email_confirmed_at;
    private String phone;
    private String confirmation_sent_at;
    private String confirmed_at;
    private String last_sign_in_at;
    private String recovery_sent_at;
    private String email_change_sent_at;
    private String new_email;
    private String new_phone;
    private String phone_confirmed_at;
    private Boolean is_anonymous;
    private String kyc_status;
    private Boolean pin_created;
    private Boolean password_created;
    private String provider_id;
    private String sign_in_provider;
    private StereumUserMetadata user_metadata;
}
