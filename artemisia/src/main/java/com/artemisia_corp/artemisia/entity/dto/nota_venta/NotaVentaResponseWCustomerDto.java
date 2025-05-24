package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaResponseWCustomerDto {
    private Long id;
    private Long userId;
    private String buyerAddress;
    private String buyerName;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;

    public NotaVentaResponseWCustomerDto(NotaVenta notaVenta) {
        this.id = notaVenta.getId();
        this.userId = notaVenta.getBuyer().getId();
        this.buyerAddress = notaVenta.getBuyerAddress().getDirection();
        this.buyerName = notaVenta.getBuyer().getName();
        this.estadoVenta = notaVenta.getEstadoVenta().toString();
        this.totalGlobal = notaVenta.getTotalGlobal();
        this.date = notaVenta.getDate();
    }
}
