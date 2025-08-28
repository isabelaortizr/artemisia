package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.user.*;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@Slf4j
@Tag(name = "Admin Dashboard", description = "Endpoints for admin dashboard analytics")
public class AdminDashboardController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotaVentaRepository notaVentaRepository;
    @Autowired
    private NotaVentaService notaVentaService;
    @Autowired
    private ProductRepository productRepository;

    @Operation(summary = "Get new users report", description = "Returns count of new users in a date range")
    @GetMapping("/new_users")
    public ResponseEntity<NewUsersReportDto> getNewUsersReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Date sDate = startDate != null ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.MIN));
        Date eDate = endDate != null ? Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.MIN));

        long newUsersCount = userRepository.countByCreatedDateBetween(
                sDate, eDate);

        return ResponseEntity.ok(new NewUsersReportDto(sDate, eDate, newUsersCount));
    }

    @Operation(summary = "Get new users report", description = "Returns count of new users in a date range")
    @GetMapping("/new_sellers")
    public ResponseEntity<NewUsersReportDto> getNewSellersReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Date sDate = startDate != null ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.MIN));
        Date eDate = endDate != null ? Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.MIN));

        long newUsersCount = userRepository.countByCreatedDateBetween(
                sDate, eDate);

        return ResponseEntity.ok(new NewUsersReportDto(sDate, eDate, newUsersCount));
    }

    @Operation(summary = "Get sales summary", description = "Returns total sales count and revenue")
    @GetMapping("/sales_summary")
    public ResponseEntity<SalesSummaryDto> getSalesSummary() {
        Double totalRevenue = notaVentaRepository.sumTotalGlobalByEstadoVenta(VentaEstado.PAYED);
        Long totalSales = notaVentaRepository.countByEstadoVenta(VentaEstado.PAYED);

        return ResponseEntity.ok(new SalesSummaryDto(totalSales, totalRevenue));
    }

    @Operation(summary = "Get top categories", description = "Returns most popular product categories")
    @GetMapping("/top_categories")
    public ResponseEntity<List<CategorySalesDto>> getTopCategories(
            @RequestParam(defaultValue = "5") int limit) {

        List<CategorySalesDto> topCategories = productRepository.findTopCategoriesBySales(limit);
        return ResponseEntity.ok(topCategories);
    }

    @Operation(summary = "Get top techniques", description = "Returns most popular painting techniques")
    @GetMapping("/top_techniques")
    public ResponseEntity<List<TechniqueSalesDto>> getTopTechniques(
            @RequestParam(defaultValue = "5") int limit) {

        List<TechniqueSalesDto> topTechniques = productRepository.findTopTechniquesBySales(limit);
        return ResponseEntity.ok(topTechniques);
    }

    @Operation(summary = "Get seller performance", description = "Returns sales performance by seller")
    @GetMapping("/seller_performance")
    public ResponseEntity<List<SellerPerformanceDto>> getSellerPerformance() {
        List<SellerPerformanceDto> performance = notaVentaRepository.findSellerPerformance();
        return ResponseEntity.ok(performance);
    }

    @Operation(summary = "Get order status summary", description = "Returns count of orders by status")
    @GetMapping("/order_status_summary")
    public ResponseEntity<Map<VentaEstado, Long>> getOrderStatusSummary() {
        Map<VentaEstado, Long> statusCounts = notaVentaRepository.countByEstadoVentaGroupByEstadoVenta();
        return ResponseEntity.ok(statusCounts);
    }

    @Operation(summary = "Get pending shipments", description = "Returns orders that need to be shipped")
    @GetMapping("/pending_shipments")
    public ResponseEntity<Page<NotaVentaResponseDto>> getPendingShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notaVentaService.getNotasVentaByEstado(VentaEstado.PAYED, pageable));
    }
}