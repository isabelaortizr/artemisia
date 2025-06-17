package com.artemisia_corp.artemisia.integracion.impl.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyConversionResponseDto {
    private String country;
    private String sourceCurrency;
    private String targetCurrency;
    private Double amount;
    private Double converted_amount;
    private Double exchange_rate;
}
