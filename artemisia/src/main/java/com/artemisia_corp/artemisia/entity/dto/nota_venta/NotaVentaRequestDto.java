package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaRequestDto {
    private Long userId;
    private Long buyerAddress;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;
    private List<OrderDetailRequestDto> detalles;
}