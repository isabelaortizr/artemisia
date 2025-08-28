package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TechniqueSalesDto {
    private PaintingTechnique technique;
    private long salesCount;
    private double totalRevenue;
}
