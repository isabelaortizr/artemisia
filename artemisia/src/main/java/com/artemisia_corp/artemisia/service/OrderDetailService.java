package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponseDto> getAllOrderDetails();
    OrderDetailResponseDto getOrderDetailById(Long id);
    OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto);
    OrderDetailResponseDto updateOrderDetail(Long id, OrderDetailRequestDto orderDetailDto);
    void deleteOrderDetail(Long id);
    List<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId);
}