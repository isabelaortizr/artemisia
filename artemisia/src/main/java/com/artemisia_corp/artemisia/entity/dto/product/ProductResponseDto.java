package com.artemisia_corp.artemisia.entity.dto.product;

import com.artemisia_corp.artemisia.entity.Product;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
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

    public ProductResponseDto(Product product) {
        this.productId = product.getId();
        this.name = product.getName();
        this.technique = product.getTechnique().name();
        this.materials = product.getMaterials();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.status = product.getStatus().name();
        this.image = product.getImageUrl();
        this.category = product.getCategory().name();
        this.sellerId = product.getSeller().getId();
    }
}
