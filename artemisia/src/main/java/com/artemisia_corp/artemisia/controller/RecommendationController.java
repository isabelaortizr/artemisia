package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation System", description = "AI-powered product recommendations")
@Slf4j
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Operation(summary = "Get personalized recommendations for user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductResponseDto>> getUserRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting recommendations for user: {}", userId);
        List<ProductResponseDto> recommendations =
                recommendationService.getUserRecommendations(userId, limit);

        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "Update user preferences (trigger vector update)")
    @PostMapping("/user/{userId}/update-preferences")
    public ResponseEntity<Void> updateUserPreferences(@PathVariable Long userId) {
        recommendationService.updateUserPreferences(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Train recommendation model (Admin only)")
    @PostMapping("/train-model")
    public ResponseEntity<Void> trainModel() {
        recommendationService.trainRecommendationModel();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find similar users")
    @GetMapping("/user/{userId}/similar")
    public ResponseEntity<List<Long>> findSimilarUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Long> similarUsers = recommendationService.findSimilarUsers(userId, limit);
        return ResponseEntity.ok(similarUsers);
    }
}
