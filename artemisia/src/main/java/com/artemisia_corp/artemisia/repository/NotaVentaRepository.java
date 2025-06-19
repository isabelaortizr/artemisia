package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    Optional<NotaVenta> findByBuyer_IdAndEstadoVenta(Long buyerId, VentaEstado estadoVenta);
}
