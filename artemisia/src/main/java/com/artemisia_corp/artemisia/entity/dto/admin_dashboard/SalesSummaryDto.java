package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SalesSummaryDto {
    private long totalSales;
    private double totalRevenue;
}
