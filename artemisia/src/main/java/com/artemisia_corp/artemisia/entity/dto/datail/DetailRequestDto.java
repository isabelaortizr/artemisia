package com.artemisia_corp.artemisia.entity.dto.datail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DetailRequestDto {
    private Long groupId;
    private Long productId;
    private Long sellerId;
    private String productName;
    private Integer quantity;
    private Double total;
}
