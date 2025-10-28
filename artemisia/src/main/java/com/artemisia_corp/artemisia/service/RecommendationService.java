package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import java.util.List;

public interface RecommendationService {
    List<ProductResponseDto> getUserRecommendations(Long userId, int limit);
    void updateUserPreferences(Long userId);
    List<Long> findSimilarUsers(Long userId, int limit);
    void trainRecommendationModel();
}
