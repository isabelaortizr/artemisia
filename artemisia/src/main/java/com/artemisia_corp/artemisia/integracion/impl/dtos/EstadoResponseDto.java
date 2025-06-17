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
public class EstadoResponseDto {
    private String id;
    private Double amount;
    private String currency;
    private String country;
    private String status;
    private Integer fee;
    @JsonProperty("created_date")
    private Long createdDate;
    @JsonProperty("status_description")
    private String statusDescription;
    @JsonProperty("on_main_net")
    private Boolean onMainNet;
    @JsonProperty("idempotency_key")
    private String idempotencyKey;
}
