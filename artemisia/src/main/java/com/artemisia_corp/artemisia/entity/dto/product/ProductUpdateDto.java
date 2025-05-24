package com.artemisia_corp.artemisia.entity.dto.product;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ProductUpdateDto {
    private Long productId;
    private String name;
    private String technique;
    private String materials;
    private String description;
    private Double price;
    private Integer stock;
    private String status;
    private String image;
    private String category;
    private Long sellerId;
}
