package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestPaymentDto {
    private String country = "BO";
    @JsonProperty("user_id")
    private Long userId;
    private String network = "POLYGON";
    private String currency;
    @JsonProperty("charge_reason")
    private String chargeReason;
}
