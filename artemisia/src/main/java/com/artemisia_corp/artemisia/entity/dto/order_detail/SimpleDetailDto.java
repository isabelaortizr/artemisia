package com.artemisia_corp.artemisia.entity.dto.order_detail;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SimpleDetailDto {
    private Long id;
    private String productName;
    private Integer quantity;
    private Double total;
    private Long notaVentaId;
}