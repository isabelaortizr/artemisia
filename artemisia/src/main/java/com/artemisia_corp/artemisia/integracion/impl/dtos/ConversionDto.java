package com.artemisia_corp.artemisia.integracion.impl.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConversionDto {
    private Long userId;
    private String originCurrency;
    private String targetCurrency;
}
