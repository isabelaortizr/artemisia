package com.artemisia_corp.artemisia.entity.dto.auction;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuctionRequestDto {
    private Long productId;
    private Long sellerId;
    private Double startingPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
