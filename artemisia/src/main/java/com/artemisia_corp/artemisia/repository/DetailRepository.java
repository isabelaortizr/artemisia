package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Detail;
import com.artemisia_corp.artemisia.entity.dto.datail.DetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.datail.SimpleDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailRepository extends JpaRepository<Detail, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.datail.DetailResponseDto(d) " +
            "FROM Detail d")
    List<DetailResponseDto> findAllDetails();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.datail.SimpleDetailDto(" +
            "d.id, d.productName, d.quantity, d.total, d.group.id) " +
            "FROM Detail d")
    List<SimpleDetailDto> findAllSimpleDetails();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.datail.SimpleDetailDto(" +
            "d.id, d.productName, d.quantity, d.total, d.group.id) " +
            "FROM Detail d WHERE d.group.id = :notaVentaId")
    List<SimpleDetailDto> findByNotaVentaId(Long notaVentaId);
}
