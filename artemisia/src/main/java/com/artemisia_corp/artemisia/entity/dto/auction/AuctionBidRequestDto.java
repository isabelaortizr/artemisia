package com.artemisia_corp.artemisia.entity.dto.auction;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuctionBidRequestDto {
    private Long auctionId;
    private Long participantId;
    private Double bidAmount;
}
