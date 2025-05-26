package com.artemisia_corp.artemisia.entity.dto.product;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ProductRequestDto {
    private Long sellerId;
    private String name;
    private String technique;
    private String materials;
    private String description;
    private Double price;
    private Integer stock;
    private String status;
    private String image;
    private String category;
}
