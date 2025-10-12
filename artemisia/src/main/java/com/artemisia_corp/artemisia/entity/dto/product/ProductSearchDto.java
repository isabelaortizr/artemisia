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
public class ProductSearchDto {
    private List<PaintingCategory> categories;
    private List<PaintingTechnique> techniques;
    private Double priceMin;
    private Double priceMax;
}