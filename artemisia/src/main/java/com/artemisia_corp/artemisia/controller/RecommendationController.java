package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation System", description = "AI-powered product recommendations")
@Slf4j
public class RecommendationController {
    private RecommendationService recommendationService;
    @Lazy
    private JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get personalized recommendations for user")
    @GetMapping("/user")
    public ResponseEntity<List<ProductResponseDto>> getUserRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        log.info("Getting recommendations for user: {}", userId);
        List<ProductResponseDto> recommendations =
                recommendationService.getUserRecommendations(userId, limit);

        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "Update user preferences (trigger vector update)")
    @PostMapping("/user/update-preferences")
    public ResponseEntity<Void> updateUserPreferences(@RequestHeader("Authorization") String token) {
        recommendationService.updateUserPreferences(jwtTokenProvider.getUserIdFromToken(token));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Train recommendation model (Admin only)")
    @PostMapping("/train-model")
    public ResponseEntity<Void> trainModel() {
        recommendationService.trainRecommendationModel();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find similar users")
    @GetMapping("/user/similar")
    public ResponseEntity<List<Long>> findSimilarUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "5") int limit) {

        List<Long> similarUsers = recommendationService.findSimilarUsers(jwtTokenProvider.getUserIdFromToken(token), limit);
        return ResponseEntity.ok(similarUsers);
    }
}
