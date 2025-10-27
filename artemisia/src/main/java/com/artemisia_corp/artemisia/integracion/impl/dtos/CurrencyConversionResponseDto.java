package com.artemisia_corp.artemisia.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("converted_amount")
    private Double convertedAmount;
    @JsonProperty("exchange_rate")
    private Double exchangeRate;
}
