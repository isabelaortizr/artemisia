package com.artemisia_corp.artemisia.utils.recommendation;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FeatureDerivationService {

    private static final Set<PaintingCategory> MODERN_CATEGORIES = Set.of(
            PaintingCategory.Abstracta,
            PaintingCategory.Contemporánea,
            PaintingCategory.Surrealista,
            PaintingCategory.Conceptual
    );

    private static final Set<PaintingCategory> TRADITIONAL_CATEGORIES = Set.of(
            PaintingCategory.Realista,
            PaintingCategory.Religiosa,
            PaintingCategory.Histórica,
            PaintingCategory.Impresionista
    );

    private static final Set<PaintingTechnique> COMPLEX_TECHNIQUES = Set.of(
            PaintingTechnique.Óleo,
            PaintingTechnique.Fresco,
            PaintingTechnique.Mixta
    );

    private static final Set<PaintingTechnique> SIMPLE_TECHNIQUES = Set.of(
            PaintingTechnique.Acuarela,
            PaintingTechnique.Tinta,
            PaintingTechnique.Digital
    );

    private static final Set<PaintingTechnique> VIBRANT_TECHNIQUES = Set.of(
            PaintingTechnique.Acrílico,
            PaintingTechnique.Óleo,
            PaintingTechnique.Spray
    );

    private static final Set<PaintingTechnique> SUBTLE_TECHNIQUES = Set.of(
            PaintingTechnique.Acuarela,
            PaintingTechnique.Temple,
            PaintingTechnique.Tinta
    );

    public void applyPricePreference(Map<String, Double> vector, Product product, double weight) {
        if (product.getPrice() == null) {
            return;
        }

        double price = product.getPrice();
        String priceKey = "price_sensitivity";
        double currentSensitivity = vector.getOrDefault(priceKey, 0.5);

        if (price < 100) {
            vector.put(priceKey, currentSensitivity + (weight * 0.8));
        } else if (price < 500) {
            vector.put(priceKey, currentSensitivity + (weight * 0.5));
        } else {
            vector.put(priceKey, currentSensitivity + (weight * 0.2));
        }
    }

    public void applyStylePreference(Map<String, Double> vector, Product product, double weight) {
        if (product.getCategories() == null) {
            return;
        }

        long modernCount = product.getCategories().stream()
                .filter(MODERN_CATEGORIES::contains)
                .count();

        long traditionalCount = product.getCategories().stream()
                .filter(TRADITIONAL_CATEGORIES::contains)
                .count();

        String styleKey = "modern_traditional";
        double currentStyle = vector.getOrDefault(styleKey, 0.5);

        if (modernCount > traditionalCount) {
            vector.put(styleKey, currentStyle + (weight * 0.7));
        } else if (traditionalCount > modernCount) {
            vector.put(styleKey, currentStyle + (weight * 0.3));
        }
    }

    public void applyTechnicalComplexityPreference(Map<String, Double> vector, Product product, double weight) {
        if (product.getTechniques() == null) {
            return;
        }

        long complexCount = product.getTechniques().stream()
                .filter(COMPLEX_TECHNIQUES::contains)
                .count();

        long simpleCount = product.getTechniques().stream()
                .filter(SIMPLE_TECHNIQUES::contains)
                .count();

        String complexityKey = "technical_complexity";
        double currentComplexity = vector.getOrDefault(complexityKey, 0.5);

        if (complexCount > simpleCount) {
            vector.put(complexityKey, currentComplexity + (weight * 0.6));
        } else if (simpleCount > complexCount) {
            vector.put(complexityKey, currentComplexity + (weight * 0.4));
        }
    }

    public void applyColorIntensityPreference(Map<String, Double> vector, Product product, double weight) {
        if (product.getTechniques() == null) {
            return;
        }

        long vibrantCount = product.getTechniques().stream()
                .filter(VIBRANT_TECHNIQUES::contains)
                .count();

        long subtleCount = product.getTechniques().stream()
                .filter(SUBTLE_TECHNIQUES::contains)
                .count();

        String colorKey = "color_intensity";
        double currentIntensity = vector.getOrDefault(colorKey, 0.5);

        if (vibrantCount > subtleCount) {
            vector.put(colorKey, currentIntensity + (weight * 0.7));
        } else if (subtleCount > vibrantCount) {
            vector.put(colorKey, currentIntensity + (weight * 0.3));
        }
    }
}