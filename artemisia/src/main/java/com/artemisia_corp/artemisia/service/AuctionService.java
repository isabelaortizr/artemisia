package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.auction.AuctionBidRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionConfirmPurchaseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionParticipantResponseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionService {
    Page<AuctionResponseDto> getAllAuctions(Pageable pageable);
    Page<AuctionResponseDto> getAuctionsByStatus(AuctionStatus status, Pageable pageable);
    Page<AuctionResponseDto> getAuctionsBySeller(Long sellerId, Pageable pageable);
    Page<AuctionResponseDto> getAuctionsWonByUser(Long winnerId, Pageable pageable);
    AuctionResponseDto getAuctionById(Long id);
    AuctionResponseDto createAuction(AuctionRequestDto auctionDto);
    AuctionResponseDto closeAuction(Long auctionId, Long sellerId);
    AuctionParticipantResponseDto placeBid(AuctionBidRequestDto bidDto);
    Page<AuctionParticipantResponseDto> getBidsByAuction(Long auctionId, Pageable pageable);
    NotaVentaResponseDto confirmAuctionPurchase(AuctionConfirmPurchaseDto confirmDto);
    void processExpiredAuctions();
}
