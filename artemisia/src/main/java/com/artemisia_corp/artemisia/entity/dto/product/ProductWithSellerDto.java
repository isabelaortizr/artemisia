package com.artemisia_corp.artemisia.entity.dto.product;

import com.artemisia_corp.artemisia.entity.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ProductWithSellerDto {
    private Long productId;
    private String name;
    private Double price;
    private Integer stock;
    private String status;
    private Long sellerId;
    private String sellerName;

    public ProductWithSellerDto(Product product, long sellerId) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.status = product.getStatus().toString();
        this.sellerId = sellerId;
        this.sellerName = product.getSeller().getName();
    }
}