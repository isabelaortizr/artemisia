package com.artemisia_corp.artemisia.entity.dto.order_detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateOrderDetailDto {
    private Long userId;
    private Long productId;
    private int quantity;
}

