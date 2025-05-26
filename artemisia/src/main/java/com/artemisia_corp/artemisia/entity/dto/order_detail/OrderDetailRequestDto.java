package com.artemisia_corp.artemisia.entity.dto.order_detail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderDetailRequestDto {
    private Long groupId;
    private Long productId;
    private Long sellerId;
    private String productName;
    private Integer quantity;
    private Double total;
}
