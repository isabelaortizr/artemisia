package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateQuantityDetailDto;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/order-details")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public ResponseEntity<Page<OrderDetailResponseDto>> getAllOrderDetails(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(orderDetailService.getAllOrderDetails(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(orderDetailService.getOrderDetailById(id));
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponseDto> createOrderDetail(@RequestBody OrderDetailRequestDto orderDetailDto) {
        OrderDetailResponseDto response = orderDetailService.createOrderDetail(orderDetailDto, null, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> updateOrderDetail(
            @PathVariable Long id,
            @RequestBody OrderDetailRequestDto orderDetailDto) {
        return ResponseEntity.ok(orderDetailService.updateOrderDetail(id, orderDetailDto));
    }

    @PutMapping("update-stock/{id}")
    public ResponseEntity<OrderDetailResponseDto> updateStockDetail(
            @RequestBody UpdateQuantityDetailDto updateQuantityDetailDto) {
        orderDetailService.updateQuantityOrderDetail(updateQuantityDetailDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nota-venta/{notaVentaId}")
    public ResponseEntity<Page<OrderDetailResponseDto>> getOrderDetailsByNotaVenta(
            @PathVariable Long notaVentaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(orderDetailService.getOrderDetailsByNotaVenta(notaVentaId, pageable));
    }
}