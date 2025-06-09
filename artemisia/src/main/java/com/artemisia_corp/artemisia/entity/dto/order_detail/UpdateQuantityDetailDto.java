package com.artemisia_corp.artemisia.entity.dto.order_detail;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Builder
@Getter
@Setter
public class UpdateQuantityDetailDto {
    private Long orderDetailId;
    private Long productId;
    private Integer quantity;

    public UpdateQuantityDetailDto(Long orderDetailId, Long productId, Integer quantity) {
        this.orderDetailId = orderDetailId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
