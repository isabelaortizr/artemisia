package com.artemisia_corp.artemisia.entity.dto.datail;

import com.artemisia_corp.artemisia.entity.Detail;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DetailResponseDto {
    private Long id;
    private Long groupId;
    private Long productId;
    private Long sellerId;
    private String productName;
    private Integer quantity;
    private Double total;

    public DetailResponseDto(Detail detail) {
        this.id = detail.getId();
        this.groupId = detail.getGroup().getId();
        this.productId = detail.getProduct().getProductId();
        this.sellerId = detail.getSeller().getId();
        this.productName = detail.getProduct().getName();
        this.quantity = detail.getQuantity();
        this.total = detail.getTotal();
    }
}
