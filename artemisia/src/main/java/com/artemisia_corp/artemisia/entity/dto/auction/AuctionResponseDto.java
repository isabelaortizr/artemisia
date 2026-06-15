package com.artemisia_corp.artemisia.entity.dto.auction;

import com.artemisia_corp.artemisia.entity.Auction;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuctionResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long sellerId;
    private String sellerName;
    private Long winnerId;
    private String winnerName;
    private Long notaVentaId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double startingPrice;
    private Double currentPrice;

    public AuctionResponseDto(Auction auction) {
        this.id = auction.getId();
        this.productId = auction.getProduct().getId();
        this.productName = auction.getProduct().getName();
        this.productImage = auction.getProduct().getImageUrl();
        this.sellerId = auction.getSeller().getId();
        this.sellerName = auction.getSeller().getName();
        this.winnerId = auction.getWinner() != null ? auction.getWinner().getId() : null;
        this.winnerName = auction.getWinner() != null ? auction.getWinner().getName() : null;
        this.notaVentaId = auction.getNotaVenta() != null ? auction.getNotaVenta().getId() : null;
        this.status = auction.getStatus().name();
        this.startDate = auction.getStartDate();
        this.endDate = auction.getEndDate();
        this.startingPrice = auction.getStartingPrice();
        this.currentPrice = auction.getCurrentPrice();
    }
}
