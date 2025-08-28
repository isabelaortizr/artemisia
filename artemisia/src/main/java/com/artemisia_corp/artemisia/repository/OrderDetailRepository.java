package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.OrderDetail;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.seller_dashboard.ProductSalesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto(dnv) " +
            "FROM OrderDetail dnv")
    Page<OrderDetailResponseDto> findAllOrderDetails(Pageable pageable);

    Page<OrderDetailResponseDto> findByGroup_Id(Long groupId, Pageable pageable);
    List<OrderDetailResponseDto> findByGroup_Id(Long groupId);

    boolean existsByGroupIdAndSellerId(Long groupId, Long sellerId);

    Optional<OrderDetail> findByGroupIdAndProductId(Long groupId, Long productId);

    @Query("SELECT SUM(od.total) FROM OrderDetail od WHERE od.group.id = :notaVentaId")
    Double calculateTotalByNotaVenta(@Param("notaVentaId") Long notaVentaId);

    @Query("SELECT SUM(od.total) FROM OrderDetail od WHERE od.seller.id = :sellerId")
    Double sumTotalBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.seller.id = :sellerId")
    Long countBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.seller_dashboard.ProductSalesDto(" +
            "od.product.id, od.productName, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od " +
            "WHERE od.seller.id = :sellerId " +
            "GROUP BY od.product.id, od.productName " +
            "ORDER BY SUM(od.total) DESC " +
            "LIMIT :limit")
    List<ProductSalesDto> findTopProductsBySeller(@Param("sellerId") Long sellerId, @Param("limit") int limit);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto(" +
            "p.category, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "WHERE od.seller.id = :sellerId " +
            "GROUP BY p.category " +
            "ORDER BY SUM(od.total) DESC")
    List<CategorySalesDto> findSalesByCategoryForSeller(@Param("sellerId") Long sellerId);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto(" +
            "p.technique, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "WHERE od.seller.id = :sellerId " +
            "GROUP BY p.technique " +
            "ORDER BY SUM(od.total) DESC")
    List<TechniqueSalesDto> findSalesByTechniqueForSeller(@Param("sellerId") Long sellerId);

    @Query("SELECT od FROM OrderDetail od WHERE od.seller.id = :sellerId")
    Page<OrderDetail> findBySellerId(
            @Param("sellerId") Long sellerId,
            Pageable pageable);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto(od) " +
            "FROM OrderDetail od WHERE od.seller.id = :sellerId")
    Page<OrderDetailResponseDto> findDtoBySellerId(
            @Param("sellerId") Long sellerId,
            Pageable pageable);
}
