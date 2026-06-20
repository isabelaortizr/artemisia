package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.AuctionParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionParticipantRepository extends JpaRepository<AuctionParticipant, Long> {

    Optional<AuctionParticipant> findByAuction_IdAndParticipant_Id(Long auctionId, Long participantId);

    Page<AuctionParticipant> findByAuction_Id(Long auctionId, Pageable pageable);

    @Query("SELECT ap FROM AuctionParticipant ap WHERE ap.auction.id = :auctionId ORDER BY ap.bidAmount DESC")
    Page<AuctionParticipant> findByAuctionIdOrderByBidAmountDesc(@Param("auctionId") Long auctionId, Pageable pageable);
}
