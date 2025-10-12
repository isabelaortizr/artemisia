package com.artemisia_corp.artemisia.entity.dto.product;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long productId;
    private String name;
    private List<String> techniques;
    private List<PaintingTechnique> techniqueEnums;
    private String materials;
    private String description;
    private Double price;
    private Integer stock;
    private String status;
    private String image;
    private List<String> categories;
    private List<PaintingCategory> categoryEnums;
    private Long sellerId;
    private String sellerName;

    // Constructor para queries que devuelven Product entero
    public ProductResponseDto(Product product) {
        this.productId = product.getId();
        this.name = product.getName();
        this.materials = product.getMaterials();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.status = product.getStatus() != null ? product.getStatus().name() : null;
        this.image = product.getImageUrl();
        this.sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        this.sellerName = product.getSeller() != null ? product.getSeller().getName() : null;

        // Convertir categorías a listas de nombres y enums
        if (product.getCategories() != null) {
            this.categories = product.getCategories().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            this.categoryEnums = new ArrayList<>(product.getCategories());
        }

        // Convertir técnicas a listas de nombres y enums
        if (product.getTechniques() != null) {
            this.techniques = product.getTechniques().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            this.techniqueEnums = new ArrayList<>(product.getTechniques());
        }
    }

    // Constructor para queries con campos específicos (opcional)
    public ProductResponseDto(Long productId, String name, String materials, String description,
                              Double price, Integer stock, ProductStatus status, String imageUrl,
                              Long sellerId, String sellerName) {
        this.productId = productId;
        this.name = name;
        this.materials = materials;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status != null ? status.name() : null;
        this.image = imageUrl;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        // Las listas de categorías y técnicas se llenarán después
        this.categories = List.of();
        this.categoryEnums = List.of();
        this.techniques = List.of();
        this.techniqueEnums = List.of();
    }
}