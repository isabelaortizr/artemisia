package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.SellerPerformanceDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface NotaVentaRepository extends JpaRepository<NotaVenta, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto(nv) " +
            "FROM NotaVenta nv")
    Page<NotaVentaResponseDto> findAllNotaVentas(Pageable pageable);

    @Query("SELECT nv FROM NotaVenta nv WHERE nv.idTransaccion =:transaction_id order by nv.id desc limit 1")
    NotaVenta findNotaVentaByIdTransaccion(@Param("transaction_id") String transactionId);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto(nv) " +
            "FROM NotaVenta nv " +
            "WHERE nv.buyer.id =:buyer_id AND nv.estadoVenta = 'PAYED'")
    Page<NotaVentaResponseDto> findAllNotaVentasByBuyer_Id(@Param("buyer_id") Long buyerId, Pageable pageable);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto(nv) " +
            "FROM NotaVenta nv WHERE nv.estadoVenta=:estado_venta")
    Page<NotaVentaResponseDto> findByEstadoVenta(@Param("estado_venta") VentaEstado estadoVenta, Pageable pageable);

    @Query("SELECT nv FROM NotaVenta nv " +
            "WHERE nv.buyer.id =:buyer_id AND nv.estadoVenta = 'ON_CART' " +
            "ORDER BY nv.id DESC LIMIT 1")
    Optional<NotaVenta> findByBuyer_IdAndEstadoVenta(@Param("buyer_id") Long buyerId);

    @Query("SELECT nv FROM NotaVenta nv " +
            "WHERE nv.buyer.id =:buyer_id " +
            "ORDER BY nv.id DESC LIMIT 1")
    Optional<NotaVenta> findLatestUsedUserCart(@Param("buyer_id") Long buyerId);

    @Query("SELECT SUM(nv.totalGlobal) FROM NotaVenta nv WHERE nv.estadoVenta = :estado")
    Double sumTotalGlobalByEstadoVenta(@Param("estado") VentaEstado estado);

    @Query("SELECT COUNT(nv) FROM NotaVenta nv WHERE nv.estadoVenta = :estado")
    Long countByEstadoVenta(@Param("estado") VentaEstado estado);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.SellerPerformanceDto(" +
            "od.seller.id, od.seller.name, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od " +
            "WHERE od.group.estadoVenta = 'PAYED' " +
            "GROUP BY od.seller.id, od.seller.name " +
            "ORDER BY SUM(od.total) DESC")
    List<SellerPerformanceDto> findSellerPerformance();

    @Query("SELECT nv.estadoVenta, COUNT(nv) FROM NotaVenta nv GROUP BY nv.estadoVenta")
    Map<VentaEstado, Long> countByEstadoVentaGroupByEstadoVenta();

    @Query("SELECT nv.estadoVenta, COUNT(nv) FROM NotaVenta nv " +
            "WHERE EXISTS (SELECT od FROM OrderDetail od WHERE od.group = nv AND od.seller.id = :sellerId) " +
            "GROUP BY nv.estadoVenta")
    Map<VentaEstado, Long> countBySellerGroupByEstadoVenta(@Param("sellerId") Long sellerId);

    @Query("SELECT nv FROM NotaVenta nv " +
            "WHERE nv.estadoVenta = :estado " +
            "AND EXISTS (SELECT od FROM OrderDetail od WHERE od.group = nv AND od.seller.id = :sellerId)")
    Page<NotaVenta> findBySellerAndEstadoVenta(
            @Param("sellerId") Long sellerId,
            @Param("estado") VentaEstado estado,
            Pageable pageable);

    @Query("SELECT COUNT(nv) FROM NotaVenta nv " +
            "WHERE nv.estadoVenta = :estado " +
            "AND EXISTS (SELECT od FROM OrderDetail od WHERE od.group = nv AND od.seller.id = :sellerId)")
    Long countBySellerAndEstado(
            @Param("sellerId") Long sellerId,
            @Param("estado") VentaEstado estado);

    @Query("SELECT nv.estadoVenta, COUNT(nv) FROM NotaVenta nv " +
            "WHERE EXISTS (SELECT od FROM OrderDetail od WHERE od.group = nv AND od.seller.id = :sellerId) " +
            "GROUP BY nv.estadoVenta")
    Map<VentaEstado, Long> countOrdersBySellerAndStatus(@Param("sellerId") Long sellerId);

    @Query("SELECT nv FROM NotaVenta nv WHERE nv.buyer.id = :buyerId AND nv.estadoVenta = :estado")
    Page<NotaVenta> findByBuyerIdAndEstadoVenta(
            @Param("buyerId") Long buyerId,
            @Param("estado") VentaEstado estado,
            Pageable pageable);

    @Query("SELECT COUNT(nv) FROM NotaVenta nv WHERE nv.buyer.id = :buyerId AND nv.estadoVenta = :estado")
    Long countByBuyerIdAndEstadoVenta(
            @Param("buyerId") Long buyerId,
            @Param("estado") VentaEstado estado);

    @Query("SELECT nv FROM NotaVenta nv WHERE nv.buyer.id = :buyerId")
    Page<NotaVenta> findByBuyerId(@Param("buyerId") Long buyerId, Pageable pageable);
}
