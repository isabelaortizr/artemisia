package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.OrderDetail;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    Optional<OrderDetail> findByGroupIdAndProductId(Long groupId, Long productId);
}
