package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaResponseDto {
    private Long id;
    private Long userId;
    private Long buyerAddress;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;
    private List<OrderDetailResponseDto> detalles;

    public NotaVentaResponseDto(NotaVenta notaVenta) {
        this.id = notaVenta.getId();
        this.userId = notaVenta.getBuyer().getId();
        this.buyerAddress = notaVenta.getBuyerAddress().getId();
        this.estadoVenta = notaVenta.getEstadoVenta().name();
        this.totalGlobal = notaVenta.getTotalGlobal();
        this.date = notaVenta.getDate();
    }
}
