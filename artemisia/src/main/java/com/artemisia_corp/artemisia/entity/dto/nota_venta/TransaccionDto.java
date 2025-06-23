package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TransaccionDto {
    private String country;
    private Integer amount;
    @JsonProperty("status_description")
    private String statusDescription;
    @JsonProperty("on_main_net")
    private Boolean onMainNet;
    private Double fee;
    @JsonProperty("idempotency_key")
    private String idempotencyKey;
    private String currency;
    private String id;
    @JsonProperty("created_date")
    private Long createdDate;
    @JsonProperty("payment_date")
    private Long paymentDate;
    private String status;
}
