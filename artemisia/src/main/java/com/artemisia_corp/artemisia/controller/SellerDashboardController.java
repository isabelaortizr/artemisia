package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.seller_dashboard.*;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.repository.OrderDetailRepository;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/dashboard")
@Tag(name = "Seller Dashboard", description = "Endpoints for seller dashboard analytics")
public class SellerDashboardController {

    @Autowired
    private NotaVentaRepository notaVentaRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private NotaVentaService notaVentaService;

    @Operation(summary = "Get seller sales summary", description = "Returns sales summary for the authenticated seller")
    @GetMapping("/sales_summary")
    public ResponseEntity<SellerSalesSummaryDto> getSellerSalesSummary(
            @RequestParam(required = false) Long sellerId) {

        Double totalRevenue = orderDetailRepository.sumTotalBySeller(sellerId);
        Long totalSales = orderDetailRepository.countBySeller(sellerId);

        // Contar órdenes pagadas que contienen productos del vendedor
        Long pendingShipments = notaVentaRepository.countBySellerAndEstado(
                sellerId,
                VentaEstado.PAYED);

        return ResponseEntity.ok(new SellerSalesSummaryDto(totalSales, totalRevenue, pendingShipments));
    }

    @Operation(summary = "Get seller's order status summary",
            description = "Returns count of seller's orders by status")
    @GetMapping("/order_status_summary")
    public ResponseEntity<Map<VentaEstado, Long>> getSellerOrderStatusSummary(
            @RequestParam(required = false) Long sellerId) {

        Map<VentaEstado, Long> statusCounts = notaVentaRepository.countOrdersBySellerAndStatus(sellerId);
        return ResponseEntity.ok(statusCounts);
    }

    @Operation(summary = "Get seller's top products", description = "Returns best selling products for the seller")
    @GetMapping("/top_products")
    public ResponseEntity<List<ProductSalesDto>> getTopProducts(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "5") int limit) {

        List<ProductSalesDto> topProducts = orderDetailRepository.findTopProductsBySeller(sellerId, limit);
        return ResponseEntity.ok(topProducts);
    }

    @Operation(summary = "Get seller's categories performance", description = "Returns sales by category for the seller")
    @GetMapping("/categories_performance")
    public ResponseEntity<List<CategorySalesDto>> getSellerCategoriesPerformance(
            @RequestParam(required = false) Long sellerId) {

        List<CategorySalesDto> categories = orderDetailRepository.findSalesByCategoryForSeller(sellerId);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get seller's techniques performance", description = "Returns sales by technique for the seller")
    @GetMapping("/techniques_performance")
    public ResponseEntity<List<TechniqueSalesDto>> getSellerTechniquesPerformance(
            @RequestParam(required = false) Long sellerId) {

        List<TechniqueSalesDto> techniques = orderDetailRepository.findSalesByTechniqueForSeller(sellerId);
        return ResponseEntity.ok(techniques);
    }

    @Operation(summary = "Get seller's pending shipments",
            description = "Returns seller's orders that need to be shipped")
    @GetMapping("/pending_shipments")
    public ResponseEntity<Page<NotaVentaResponseDto>> getSellerPendingShipments(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotaVentaResponseDto> pendingShipments =
                notaVentaService.getNotasVentaBySellerAndEstado(sellerId, VentaEstado.PAYED, pageable);
        return ResponseEntity.ok(pendingShipments);
    }

    @Operation(summary = "Mark order as shipped",
            description = "Updates order status to SHIPPED")
    @PutMapping("/mark_shipped/{notaVentaId}")
    public ResponseEntity<NotaVentaResponseDto> markAsShipped(
            @PathVariable Long notaVentaId,
            @RequestParam(required = false) Long sellerId) {

        NotaVentaResponseDto response = notaVentaService.markNotaVentaAsShipped(notaVentaId, sellerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent orders", description = "Returns recent orders for seller's products")
    @GetMapping("/recent_orders")
    public ResponseEntity<Page<OrderDetailResponseDto>> getRecentOrders(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(orderDetailService.getOrderDetailsBySeller(sellerId, pageable));
    }
}