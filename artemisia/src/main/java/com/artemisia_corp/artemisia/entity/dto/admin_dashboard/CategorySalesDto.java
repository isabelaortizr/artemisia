package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CategorySalesDto {
    private PaintingCategory category;
    private long salesCount;
    private double totalRevenue;
}
