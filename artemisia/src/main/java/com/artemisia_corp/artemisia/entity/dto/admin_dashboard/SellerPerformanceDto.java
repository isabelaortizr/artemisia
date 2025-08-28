package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SellerPerformanceDto {
    private Long sellerId;
    private String sellerName;
    private long salesCount;
    private double totalRevenue;
}
