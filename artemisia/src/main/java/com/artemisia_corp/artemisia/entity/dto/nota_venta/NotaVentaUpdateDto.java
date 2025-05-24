package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaUpdateDto {
    private Long id;
    private Long userId;
    private Long buyerAddress;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;
}
