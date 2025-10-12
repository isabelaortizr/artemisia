package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechniqueSalesDto {
    private PaintingTechnique technique;
    private Long salesCount;
    private Double totalRevenue;

    public String getTechniqueName() {
        return technique != null ? technique.name() : "";
    }
}