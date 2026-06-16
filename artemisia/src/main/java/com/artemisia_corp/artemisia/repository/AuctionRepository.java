package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Auction;
import com.artemisia_corp.artemisia.entity.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);

    Page<Auction> findBySeller_Id(Long sellerId, Pageable pageable);

    Page<Auction> findByWinner_Id(Long winnerId, Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endDate <= :now")
    List<Auction> findExpiredActiveAuctions(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Auction a WHERE a.product.id = :productId AND a.status = 'ACTIVE'")
    Optional<Auction> findActiveAuctionByProductId(@Param("productId") Long productId);
}
