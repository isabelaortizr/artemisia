package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaRequestDto {
    private Long buyerId;
    private Long buyerAddressId;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;
}
