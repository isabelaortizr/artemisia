package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.ProductService;
import com.artemisia_corp.artemisia.service.RecommendationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.artemisia_corp.artemisia.service.impl.clients.RecommenderPythonClient;

import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final ProductService productService;
    private final LogsService logsService;
    private final RecommenderPythonClient recommenderClient;

    @Override
    @Transactional
    public List<ProductResponseDto> getUserRecommendations(Long userId, int limit) {
        try {
            // Delegate recommendation retrieval to the Python service (returns product ids)
            Integer[] recIds = recommenderClient.getRecommendationIds(userId.intValue(), limit);

            if (recIds != null && recIds.length > 0) {
                List<ProductResponseDto> mapped = new ArrayList<>();
                for (Integer pid : recIds) {
                    if (pid == null) continue;
                    try {
                        mapped.add(productService.getProductById(pid.longValue()));
                    } catch (Exception e) {
                        if (logsService != null) logsService.error(e.getMessage());
                    }
                }
                if (!mapped.isEmpty()) return mapped;
            }

            // Fallback: popular products
            return getPopularProducts(limit, userId);

        } catch (Exception e) {
            log.error("Error getting recommendations for user {}: {}", userId, e.getMessage());
            return getPopularProducts(limit, userId);
        }
    }

    @Override
    @Async
    @Transactional
    public void updateUserPreferences(Long userId) {
        try {
            // Delegate preference update / retrain trigger to Python service
            // This will ask the Python service to start a retrain (or handle update logic)
            recommenderClient.train(null);
            log.info("Requested preference update / retrain for user {} to ML service", userId);

        } catch (Exception e) {
            log.error("Error updating preferences for user {}: {}", userId, e.getMessage());
        }
    }

    

    @Override
    public List<Long> findSimilarUsers(Long userId, int limit) {
        try {
            Long[] similarUsers = recommenderClient.getSimilarUsersAsArray(userId.intValue(), limit);
            return similarUsers != null ? Arrays.asList(similarUsers) : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error finding similar users: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void trainRecommendationModel() {
        try {
            // Delegate training trigger to the Python service
            String resp = recommenderClient.train(null);
            log.info("Requested training on ML service, resp={}", resp);

        } catch (Exception e) {
            log.error("Error training recommendation model: {}", e.getMessage());
        }
    }

    private List<ProductResponseDto> getPopularProducts(int limit, long userId) {
        Pageable pageable = Pageable.ofSize(limit);
        return productService.getAvailableProducts(pageable, userId).getContent();
    }
} 