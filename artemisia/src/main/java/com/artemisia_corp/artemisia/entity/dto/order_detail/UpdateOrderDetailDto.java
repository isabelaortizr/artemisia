package com.artemisia_corp.artemisia.entity.dto.order_detail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UpdateOrderDetailDto {
    private Long userId;
    private Long productId;
    private int quantity;
}

