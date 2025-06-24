package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateQuantityDetailDto;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order Detail Management", description = "Endpoints for managing order details")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    @Operation(summary = "Get all order details", description = "Returns paginated list of all order details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<OrderDetailResponseDto>> getAllOrderDetails(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(orderDetailService.getAllOrderDetails(pageable));
    }

    @Operation(summary = "Get order detail by ID", description = "Returns a single order detail by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order detail found",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order detail not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(orderDetailService.getOrderDetailById(id));
    }

    @Operation(summary = "Create a new order detail", description = "Creates a new order detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order detail created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<OrderDetailResponseDto> createOrderDetail(@RequestBody OrderDetailRequestDto orderDetailDto) {
        OrderDetailResponseDto response = orderDetailService.createOrderDetail(orderDetailDto, null, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an order detail", description = "Updates an existing order detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order detail updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> updateOrderDetail(
            @PathVariable Long id,
            @RequestBody OrderDetailRequestDto orderDetailDto) {
        return ResponseEntity.ok(orderDetailService.updateOrderDetail(id, orderDetailDto));
    }

    @Operation(summary = "Update order detail stock", description = "Updates the quantity of an order detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("update-stock/{id}")
    public ResponseEntity<OrderDetailResponseDto> updateStockDetail(
            @RequestBody UpdateQuantityDetailDto updateQuantityDetailDto) {
        orderDetailService.updateQuantityOrderDetail(updateQuantityDetailDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete an order detail", description = "Deletes an order detail by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order detail deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order detail not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get order details by sales note", description = "Returns paginated list of order details for a specific sales note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Sales note not found")
    })
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