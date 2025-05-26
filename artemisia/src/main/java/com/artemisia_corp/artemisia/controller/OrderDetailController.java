package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @GetMapping
    public ResponseEntity<List<OrderDetailResponseDto>> getAllOrderDetails() {
        return ResponseEntity.ok(orderDetailService.getAllOrderDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(orderDetailService.getOrderDetailById(id));
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponseDto> createOrderDetail(@RequestBody OrderDetailRequestDto orderDetailDto) {
        OrderDetailResponseDto response = orderDetailService.createOrderDetail(orderDetailDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> updateOrderDetail(
            @PathVariable Long id,
            @RequestBody OrderDetailRequestDto orderDetailDto) {
        return ResponseEntity.ok(orderDetailService.updateOrderDetail(id, orderDetailDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nota-venta/{notaVentaId}")
    public ResponseEntity<List<OrderDetailResponseDto>> getOrderDetailsByNotaVenta(
            @PathVariable Long notaVentaId) {
        return ResponseEntity.ok(orderDetailService.getOrderDetailsByNotaVenta(notaVentaId));
    }
}