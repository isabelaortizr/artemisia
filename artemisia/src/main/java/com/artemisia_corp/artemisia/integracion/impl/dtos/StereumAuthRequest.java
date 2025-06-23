package com.artemisia_corp.artemisia.integracion.impl.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StereumAuthRequest {
    private String username;
    private String password;
}
