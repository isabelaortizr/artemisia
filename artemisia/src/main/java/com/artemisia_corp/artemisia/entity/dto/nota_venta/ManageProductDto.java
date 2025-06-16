package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.*;

@NoArgsConstructor
@Builder
@Getter
@Setter
public class ManageProductDto {
    private Long productId;
    private int quantity;
    private boolean reduceStock = true;

    public ManageProductDto(Long productId, int quantity, boolean reduceStock) {
        this.productId = productId;
        this.quantity = quantity;
        this.reduceStock = reduceStock;
    }

    public ManageProductDto(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
