package com.artemisia_corp.artemisia.integracion.impl.dtos.StereumUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    private String birthdate;
    @JsonProperty("document_number")
    private String documentNumber;
    @JsonProperty("second_surname")
    private String secondSurname;
    @JsonProperty("_2fa_biometrics")
    private Boolean twoFaBiometrics;
    @JsonProperty("first_surname")
    private String firstSurname;
    @JsonProperty("locale_code")
    private String localeCode;
    @JsonProperty("user_type")
    private String userType;
    @JsonProperty("phone_verified")
    private Boolean phoneVerified;
    @JsonProperty("terms_conditions")
    private String termsConditions;
    private String name;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String exp;
    private String email;
    private String username;
    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("ACTIVO")
    private String activo;
    @JsonProperty("privacy_policies")
    private String privacyPolicies;
}
