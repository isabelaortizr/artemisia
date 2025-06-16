package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaVentaRepository extends JpaRepository<NotaVenta, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto(nv) " +
            "FROM NotaVenta nv")
    List<NotaVentaResponseDto> findAllNotaVentas();

    List<NotaVentaResponseDto> findAllNotaVentasByBuyer_Id(Long buyerId);

    List<NotaVenta> findByEstadoVenta(VentaEstado estadoVenta);
}
