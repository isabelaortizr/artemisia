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
    private Long userId;      // ID del usuario
    private Long productId;   // ID del producto
    private int quantity = 1;
}

