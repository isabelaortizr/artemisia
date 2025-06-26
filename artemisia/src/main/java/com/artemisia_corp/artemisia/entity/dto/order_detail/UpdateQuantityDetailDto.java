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
    private Integer quantity;

    public UpdateQuantityDetailDto(Long orderDetailId, Integer quantity) {
        this.orderDetailId = orderDetailId;
        this.quantity = quantity;
    }
}
