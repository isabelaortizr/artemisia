package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartDto {
    private Long userId;
    private Long productId;
    private Integer quantity;
}