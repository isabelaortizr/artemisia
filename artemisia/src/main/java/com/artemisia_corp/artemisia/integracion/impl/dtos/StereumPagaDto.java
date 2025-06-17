package com.artemisia_corp.artemisia.integracion.impl.dtos;

import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumUtils.StereumCustomerData;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StereumPagaDto {
    private String country;
    private String amount;
    private String network;
    private String currency;
    @JsonProperty("idempotency_key")
    private String idempotencyKey;
    @JsonProperty("charge_reason")
    private String chargeReason;
    private String callback;
    private StereumCustomerData customer;
}
