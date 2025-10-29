package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.service.ProductViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/product-views")
@Tag(name = "Product View Tracking", description = "Track and manage product views for recommendations")
@Slf4j
public class ProductViewController {

    private final ProductViewService productViewService;
    private JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Track a product view")
    @PostMapping("/track/{productId}")
    public ResponseEntity<Void> trackProductView(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        productViewService.trackProductView(userId, productId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Initial product interests by user")
    @PostMapping("/track/first_login")
    public ResponseEntity<Void> trackFirstLogin(
            @RequestBody List<Long> productIds,
            @RequestHeader("Authorization") String token
    ) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        productViewService.trackUserFirstLogin(userId, productIds);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Track a product view with duration")
    @PostMapping("/track/{productId}/duration")
    public ResponseEntity<Void> trackProductViewWithDuration(
            @PathVariable Long productId,
            @RequestParam Integer durationSeconds,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        productViewService.trackProductViewWithDuration(userId, productId, durationSeconds);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user's recently viewed products")
    @GetMapping("/recent")
    public ResponseEntity<List<ProductResponseDto>> getRecentViews(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<ProductResponseDto> recentViews =
                productViewService.getUserRecentlyViewedProducts(userId, limit);
        return ResponseEntity.ok(recentViews);
    }

    @Operation(summary = "Get user's most viewed products")
    @GetMapping("/most-viewed")
    public ResponseEntity<List<ProductResponseDto>> getMostViews(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<ProductResponseDto> mostViews =
                productViewService.getUserMostViewedProducts(userId, limit);
        return ResponseEntity.ok(mostViews);
    }

    @Operation(summary = "Get user view statistics")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getViewStatistics(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        Map<String, Object> statistics = productViewService.getUserViewStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Check if user has viewed a product")
    @GetMapping("/has-viewed/{productId}")
    public ResponseEntity<Boolean> hasViewedProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        boolean hasViewed = productViewService.hasUserViewedProduct(userId, productId);
        return ResponseEntity.ok(hasViewed);
    }

    @Operation(summary = "Clean up old views (Admin only)")
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldViews(@RequestParam(defaultValue = "90") int daysToKeep) {
        productViewService.cleanupOldViews(daysToKeep);
        return ResponseEntity.ok().build();
    }
}
