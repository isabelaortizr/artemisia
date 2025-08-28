package com.artemisia_corp.artemisia.entity.dto.seller_dashboard;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProductSalesDto {
    private Long productId;
    private String productName;
    private long salesCount;
    private double totalRevenue;
}
