package com.artemisia_corp.artemisia.entity.dto.auction;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuctionConfirmPurchaseDto {
    private Long auctionId;
    private Long buyerId;
    private Long addressId;
}
