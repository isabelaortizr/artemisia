package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateQuantityDetailDto;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponseDto> getAllOrderDetails();
    OrderDetailResponseDto getOrderDetailById(Long id);
    OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto, NotaVenta notaVentaParam, Product productParam);
    OrderDetailResponseDto updateOrderDetail(Long id, OrderDetailRequestDto orderDetailDto);
    void updateQuantityOrderDetail(UpdateQuantityDetailDto updateDetailDto);
    void deleteOrderDetail(Long id);
    List<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId);
}
