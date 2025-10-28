package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.ProductView;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.ProductViewRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.ProductService;
import com.artemisia_corp.artemisia.service.ProductViewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import com.artemisia_corp.artemisia.service.impl.clients.RecommenderPythonClient;

@Slf4j
@Service
@AllArgsConstructor
public class ProductViewServiceImpl implements ProductViewService {
    private final ProductViewRepository productViewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final RecommenderPythonClient recommenderClient;

    @Override
    @Async
    @Transactional
    public void trackProductView(Long userId, Long productId) {
        try {
            trackProductViewInternal(userId, productId, null);
        } catch (Exception e) {
            log.error("Error tracking product view for user {} product {}: {}",
                    userId, productId, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public void trackProductViewWithDuration(Long userId, Long productId, Integer durationSeconds) {
        try {
            trackProductViewInternal(userId, productId, durationSeconds);
        } catch (Exception e) {
            log.error("Error tracking product view with duration for user {} product {}: {}",
                    userId, productId, e.getMessage());
        }
    }

    private void trackProductViewInternal(Long userId, Long productId, Integer durationSeconds) {
        try {
            // Verificar que el usuario y producto existen
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotDataFoundException("User not found with ID: " + userId));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotDataFoundException("Product not found with ID: " + productId));

            // Buscar vista existente o crear nueva
            Optional<ProductView> existingView = productViewRepository.findByUserIdAndProductId(userId, productId);

            ProductView productView;
            if (existingView.isPresent()) {
                productView = existingView.get();
                productView.incrementViewCount();
                if (durationSeconds != null && durationSeconds > 0) {
                    productView.addViewDuration(durationSeconds);
                }
            } else {
                productView = ProductView.builder()
                        .user(user)
                        .product(product)
                        .viewCount(1)
                        .totalViewDuration(durationSeconds != null ? durationSeconds : 0)
                        .firstViewedAt(LocalDateTime.now())
                        .lastViewedAt(LocalDateTime.now())
                        .build();
            }

            productViewRepository.save(productView);
            log.debug("Tracked product view - User: {}, Product: {}, Views: {}",
                    userId, productId, productView.getViewCount());

            try {
                Integer pid = productId != null ? productId.intValue() : null;
                recommenderClient.notifyView(userId.intValue(), pid, durationSeconds);
            } catch (Exception ex) {
                log.warn("Failed to notify recommender service about view: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("Error in trackProductViewInternal for user {} product {}: {}",
                    userId, productId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getUserRecentlyViewedProducts(Long userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ProductView> recentViews = productViewRepository.findByUserIdOrderByLastViewedAtDesc(userId, pageable)
                    .getContent();

            return recentViews.stream()
                    .map(ProductView::getProduct)
                    .map(product -> productService.getProductById(product.getId()))
                    .toList();
        } catch (Exception e) {
            log.error("Error getting recently viewed products for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getUserMostViewedProducts(Long userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ProductView> topViews = productViewRepository.findTopViewedByUserId(userId, pageable);

            return topViews.stream()
                    .map(ProductView::getProduct)
                    .map(product -> productService.getProductById(product.getId()))
                    .toList();
        } catch (Exception e) {
            log.error("Error getting most viewed products for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Double> getUserViewWeights(Long userId) {
        try {
            List<ProductView> userViews = productViewRepository.findRecentViewsByUser(
                    userId, LocalDateTime.now().minusDays(30));

            Map<Long, Double> viewWeights = new HashMap<>();

            if (userViews.isEmpty()) {
                return viewWeights;
            }

            // Calcular pesos basados en múltiples factores
            int maxViewCount = userViews.stream()
                    .mapToInt(ProductView::getViewCount)
                    .max()
                    .orElse(1);

            long totalViewDuration = productViewRepository.getTotalViewDurationByUser(userId);
            double avgViewDuration = totalViewDuration > 0 ?
                    (double) totalViewDuration / userViews.size() : 0;

            LocalDateTime now = LocalDateTime.now();

            for (ProductView view : userViews) {
                double weight = calculateViewWeight(view, maxViewCount, avgViewDuration, now);
                viewWeights.put(view.getProduct().getId(), weight);
            }

            // Normalizar pesos
            normalizeWeights(viewWeights);

            return viewWeights;
        } catch (Exception e) {
            log.error("Error getting user view weights for user {}: {}", userId, e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public void cleanupOldViews(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            int deletedCount = productViewRepository.deleteOldViews(cutoffDate);
            log.info("Cleaned up {} old product views older than {} days", deletedCount, daysToKeep);
        } catch (Exception e) {
            log.error("Error cleaning up old views: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getSimilarProductsBasedOnViews(Long userId, Long productId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return productViewRepository.findSimilarViewedProducts(userId, productId, pageable);
        } catch (Exception e) {
            log.error("Error getting similar products based on views for user {} product {}: {}",
                    userId, productId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserViewStatistics(Long userId) {
        try {
            Object[] stats = productViewRepository.getUserViewStatistics(userId);

            Map<String, Object> statistics = new HashMap<>();

            if (stats != null && stats.length >= 3) {
                statistics.put("totalViewedProducts", safeExtractLong(stats[0]));
                statistics.put("totalViewCount", safeExtractLong(stats[1]));
                statistics.put("totalViewDuration", safeExtractLong(stats[2]));
            } else {
                statistics.put("totalViewedProducts", 0L);
                statistics.put("totalViewCount", 0L);
                statistics.put("totalViewDuration", 0L);
            }

            return statistics;
        } catch (Exception e) {
            log.error("Error getting user view statistics for user {}: {}", userId, e.getMessage());
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalViewedProducts", 0L);
            emptyStats.put("totalViewCount", 0L);
            emptyStats.put("totalViewDuration", 0L);
            return emptyStats;
        }
    }

    private Long safeExtractLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof Object[]) {
            // Handle array case - take first element if available
            Object[] array = (Object[]) value;
            if (array.length > 0 && array[0] instanceof Number) {
                return ((Number) array[0]).longValue();
            }
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserViewedProduct(Long userId, Long productId) {
        try {
            return productViewRepository.existsByUserIdAndProductId(userId, productId);
        } catch (Exception e) {
            log.error("Error checking if user viewed product for user {} product {}: {}",
                    userId, productId, e.getMessage());
            return false;
        }
    }

    private double calculateViewWeight(ProductView view, int maxViewCount, double avgViewDuration, LocalDateTime now) {
        double weight = 0.0;

        // Factor 1: Frecuencia de vistas (40%)
        double frequencyWeight = (double) view.getViewCount() / maxViewCount * 0.4;
        weight += frequencyWeight;

        // Factor 2: Tiempo de visualización (30%)
        double durationWeight = 0.0;
        if (avgViewDuration > 0 && view.getTotalViewDuration() > 0) {
            double durationRatio = Math.min((double) view.getTotalViewDuration() / avgViewDuration, 3.0);
            durationWeight = (durationRatio / 3.0) * 0.3;
        }
        weight += durationWeight;

        // Factor 3: Recientismo (30%)
        long daysSinceView = java.time.Duration.between(view.getLastViewedAt(), now).toDays();
        double recencyWeight = Math.max(0, 1.0 - (daysSinceView / 30.0)) * 0.3;
        weight += recencyWeight;

        return weight;
    }

    private void normalizeWeights(Map<Long, Double> weights) {
        double maxWeight = weights.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        if (maxWeight > 0) {
            for (Map.Entry<Long, Double> entry : weights.entrySet()) {
                entry.setValue(entry.getValue() / maxWeight);
            }
        }
    }
}