package com.artemisia_corp.artemisia.entity.dto.product;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Min(1)
    private Double price;
    @Min(1)
    private Integer stock;
    private String status;
    @NotNull
    @NotBlank
    private String image;
    private Long sellerId;
    private List<PaintingCategory> categories;
    private List<PaintingTechnique> techniques;
}