package com.artemisia_corp.artemisia.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StereumPagaResponseDto {
    private Double amount;
    private String currency;
    private String network;
    private String id;
    @JsonProperty("qr_base64")
    private String qrBase64;
    @JsonProperty("payment_link")
    private String paymentLink;
    @JsonProperty("transaction_status")
    private String transactionStatus;
    @JsonProperty("on_main_net")
    private String onMainNet;
    @JsonProperty("collecting_account")
    private String collectingAccount;
    @JsonProperty("expiration_time")
    private Long expirationTime;
}
