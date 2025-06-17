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
public class StereumCustomerData {
    private String name;
    private String lastname;
    @JsonProperty("document_number")
    private String documentNumber;
    private String email;
    private String phone;
    private String address;
    private String city = "Santa Cruz de la Sierra";
    private String country = "BO";
    private String state = "Santa Cruz";
    private String zip_code = "0000";
}
