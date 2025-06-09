package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.OrderDetail;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto(dnv) " +
            "FROM OrderDetail dnv")
    List<OrderDetailResponseDto> findAllOrderDetails();

    List<OrderDetailResponseDto> findByGroup_Id(Long groupId);
}
