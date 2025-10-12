package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesDto {
    private PaintingCategory category;
    private Long salesCount;
    private Double totalRevenue;

    public String getCategoryName() {
        return category != null ? category.name() : "";
    }
}