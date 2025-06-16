package com.artemisia_corp.artemisia.entity.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDto {
    private String category;
    private String technique;
    private Double priceMin;
    private Double priceMax;
}

