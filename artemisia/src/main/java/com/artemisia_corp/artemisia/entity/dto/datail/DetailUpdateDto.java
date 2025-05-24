package com.artemisia_corp.artemisia.entity.dto.datail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DetailUpdateDto {
    private Long id;
    private Long groupId;
    private Long productId;
    private Long sellerId;
    private String productName;
    private Integer quantity;
    private Double total;
}
