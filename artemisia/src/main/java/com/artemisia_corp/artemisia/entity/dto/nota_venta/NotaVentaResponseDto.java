package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotaVentaResponseDto {
    private Long id;
    private Long buyerId;
    private Long buyerAddressId;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;

    public NotaVentaResponseDto(NotaVenta notaVenta) {
        this.id = notaVenta.getId();
        this.buyerId = notaVenta.getBuyer().getId();
        this.buyerAddressId = notaVenta.getBuyerAddress().getAddressId();
        this.estadoVenta = notaVenta.getEstadoVenta().name();
        this.totalGlobal = notaVenta.getTotalGlobal();
        this.date = notaVenta.getDate();
    }
}
