package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;

import java.util.List;
import java.util.Map;

public interface ProductViewService {
    void trackProductView(Long userId, Long productId);
    void trackProductViewWithDuration(Long userId, Long productId, Integer durationSeconds);
    List<ProductResponseDto> getUserRecentlyViewedProducts(Long userId, int limit);
    List<ProductResponseDto> getUserMostViewedProducts(Long userId, int limit);
    Map<Long, Double> getUserViewWeights(Long userId);
    void cleanupOldViews(int daysToKeep);
    List<Long> getSimilarProductsBasedOnViews(Long userId, Long productId, int limit);
    Map<String, Object> getUserViewStatistics(Long userId);
    boolean hasUserViewedProduct(Long userId, Long productId);
}