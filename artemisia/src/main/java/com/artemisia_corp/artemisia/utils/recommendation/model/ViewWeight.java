package com.artemisia_corp.artemisia.utils.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewWeight {
    private Long productId;
    private Integer viewCount;
    private Integer totalDuration;
    private LocalDateTime lastViewed;
    private LocalDateTime firstViewed;
    private Double calculatedWeight;
}
