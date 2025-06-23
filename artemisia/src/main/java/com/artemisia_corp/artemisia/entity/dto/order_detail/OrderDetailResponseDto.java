package com.artemisia_corp.artemisia.entity.dto.order_detail;

import com.artemisia_corp.artemisia.entity.OrderDetail;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderDetailResponseDto {
    private Long id;
    private Long groupId;
    private Long productId;
    private Long sellerId;
    private String productName;
    private Integer quantity;
    private Double total;

    public OrderDetailResponseDto(OrderDetail detalleVenta) {
        this.id = detalleVenta.getId();
        this.groupId = detalleVenta.getGroup().getId();
        this.productId = detalleVenta.getProduct().getId();
        this.sellerId = detalleVenta.getSeller().getId();
        this.productName = detalleVenta.getProduct().getName();
        this.quantity = detalleVenta.getQuantity();
        this.total = detalleVenta.getTotal();
    }
}
