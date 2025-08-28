package com.artemisia_corp.artemisia.entity.dto.seller_dashboard;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SellerSalesSummaryDto {
    private long totalSales;
    private double totalRevenue;
    private long pendingShipments;
}
