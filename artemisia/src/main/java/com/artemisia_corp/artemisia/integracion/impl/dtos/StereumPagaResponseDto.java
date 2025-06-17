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
}
