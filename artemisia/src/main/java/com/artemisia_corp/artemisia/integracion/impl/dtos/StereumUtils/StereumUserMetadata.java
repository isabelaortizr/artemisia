package com.artemisia_corp.artemisia.integracion.impl.dtos.StereumUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StereumUserMetadata {
    private String sub;
    private String country;
    private Boolean email_verified;
    private String birthdate;
    private String document_number;
    private String second_surname;
    private Boolean _2fa_biometrics;
    private String first_surname;
    private String locale_code;
    private String user_type;
    private Boolean phone_verified;
    private String terms_conditions;
    private String name;
    private String phone_number;
    private String exp;
    private String email;
    private String username;
    private String document_type;
    private String ACTIVO;
    private String privacy_policies;
}
