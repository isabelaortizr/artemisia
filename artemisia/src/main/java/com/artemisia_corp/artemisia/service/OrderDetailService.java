package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateQuantityDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderDetailService {
    Page<OrderDetailResponseDto> getAllOrderDetails(Pageable pageable);
    OrderDetailResponseDto getOrderDetailById(Long id);
    OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto, NotaVenta notaVentaParam, Product productParam);
    OrderDetailResponseDto updateOrderDetail(Long id, OrderDetailRequestDto orderDetailDto);
    void updateQuantityOrderDetail(UpdateQuantityDetailDto updateDetailDto);
    void deleteOrderDetail(Long id);
    Page<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId, Pageable pageable);
    List<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId);
    Page<OrderDetailResponseDto> getOrderDetailsBySeller(Long sellerId, Pageable pageable);
}
