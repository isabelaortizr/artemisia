package com.artemisia_corp.artemisia.integracion.impl.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyConversionDto {
    private String country = "BO";
    private String sourceCurrency;
    private String targetCurrency;
    private Double amount;

    public CurrencyConversionDto(String sourceCurrency, String targetCurrency, Double amount) {
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.amount = amount;
    }
}
