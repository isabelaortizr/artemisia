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
    private String idTransaccion;
    private List<OrderDetailResponseDto> detalles;
    private String monedaCarrito;

    public NotaVentaResponseDto(NotaVenta notaVenta) {
        this.id = notaVenta.getId();
        this.userId = notaVenta.getBuyer().getId();
        this.buyerAddress = notaVenta.getBuyerAddress() != null ? notaVenta.getBuyerAddress().getId() : null;
        this.estadoVenta = notaVenta.getEstadoVenta().name();
        this.totalGlobal = notaVenta.getTotalGlobal();
        this.idTransaccion = notaVenta.getIdTransaccion();
        this.date = notaVenta.getDate();
        this.monedaCarrito = notaVenta.getMonedaCarrito();
    }
}