package com.artemisia_corp.artemisia.utils.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductViewData {
    private Long productId;
    private Object[] rawViewData;
    private Double normalizedWeight;
}
