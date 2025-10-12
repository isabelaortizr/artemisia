package com.artemisia_corp.artemisia.entity.dto.product;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
    private String name;
    private String materials;
    private String description;
    private Double price;
    private Integer stock;
    private String status;
    private String image;
    private Long sellerId;
    private List<PaintingCategory> categories;
    private List<PaintingTechnique> techniques;
}