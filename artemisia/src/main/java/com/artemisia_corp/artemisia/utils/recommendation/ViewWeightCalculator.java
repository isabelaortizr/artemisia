package com.artemisia_corp.artemisia.utils.recommendation;

import com.artemisia_corp.artemisia.utils.recommendation.model.ViewWeight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ViewWeightCalculator {

    public Map<Long, Double> calculateViewWeights(List<Object[]> viewHistory) {
        Map<Long, Double> weights = new HashMap<>();

        if (viewHistory.isEmpty()) {
            return weights;
        }

        // Encontrar máximos para normalización
        int maxViewCount = 0;
        int maxTotalDuration = 0;
        LocalDateTime mostRecent = LocalDateTime.now().minusYears(1);

        for (Object[] view : viewHistory) {
            Integer viewCount = (Integer) view[1];
            Integer totalDuration = (Integer) view[2];
            LocalDateTime lastViewed = (LocalDateTime) view[3];

            maxViewCount = Math.max(maxViewCount, viewCount);
            maxTotalDuration = Math.max(maxTotalDuration, totalDuration);
            if (lastViewed.isAfter(mostRecent)) {
                mostRecent = lastViewed;
            }
        }

        // Asegurar que no dividamos por cero
        maxViewCount = Math.max(maxViewCount, 1);
        maxTotalDuration = Math.max(maxTotalDuration, 1);

        // Calcular peso para cada vista
        for (Object[] view : viewHistory) {
            Long productId = ((Number) view[0]).longValue();
            Integer viewCount = (Integer) view[1];
            Integer totalDuration = (Integer) view[2];
            LocalDateTime lastViewed = (LocalDateTime) view[3];
            LocalDateTime firstViewed = (LocalDateTime) view[4];

            double weight = calculateSingleViewWeight(
                    productId, viewCount, totalDuration, lastViewed, firstViewed,
                    maxViewCount, maxTotalDuration, mostRecent
            );

            weights.put(productId, weight);
        }

        // Normalizar pesos entre 0 y 1
        return normalizeWeights(weights);
    }

    private double calculateSingleViewWeight(Long productId, int viewCount, int totalDuration,
                                             LocalDateTime lastViewed,
                                             LocalDateTime firstViewed,
                                             int maxViewCount, int maxTotalDuration,
                                             LocalDateTime mostRecent) {
        double weight = 0.0;

        // Factor 1: Frecuencia de vistas (40% del peso)
        double frequencyWeight = (double) viewCount / maxViewCount * 0.4;
        weight += frequencyWeight;

        // Factor 2: Tiempo total de visualización (30% del peso)
        double durationWeight = (double) totalDuration / maxTotalDuration * 0.3;
        weight += durationWeight;

        // Factor 3: Recientismo (20% del peso)
        long daysSinceView = java.time.Duration.between(lastViewed, mostRecent).toDays();
        double recencyDecay = Math.max(0, 1.0 - (daysSinceView / 30.0));
        double recencyWeight = recencyDecay * 0.2;
        weight += recencyWeight;

        // Factor 4: Consistencia temporal (10% del peso)
        long totalViewPeriod = java.time.Duration.between(firstViewed, lastViewed).toDays();
        double consistencyWeight = 0.0;
        if (totalViewPeriod > 0 && viewCount > 1) {
            double viewsPerDay = (double) viewCount / totalViewPeriod;
            consistencyWeight = Math.min(viewsPerDay * 0.1, 0.1);
        }
        weight += consistencyWeight;

        log.debug("Product {} - Frequency: {:.3f}, Duration: {:.3f}, Recency: {:.3f}, Consistency: {:.3f}, Total: {:.3f}",
                productId, frequencyWeight, durationWeight, recencyWeight, consistencyWeight, weight);

        return weight;
    }

    private Map<Long, Double> normalizeWeights(Map<Long, Double> weights) {
        if (weights.isEmpty()) {
            return weights;
        }

        double maxWeight = weights.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        if (maxWeight > 0) {
            for (Map.Entry<Long, Double> entry : weights.entrySet()) {
                entry.setValue(entry.getValue() / maxWeight);
            }
        }

        return weights;
    }
}