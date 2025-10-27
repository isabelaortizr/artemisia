package com.artemisia_corp.artemisia.utils.recommendation;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewPreferenceApplier {

    private final FeatureDerivationService featureDerivationService;

    public void applyProductCategoriesToVector(Map<String, Double> vector, Product product, double weight) {
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            return;
        }

        for (PaintingCategory category : product.getCategories()) {
            String key = "cat_" + category.name();
            double currentValue = vector.getOrDefault(key, 0.0);
            vector.put(key, currentValue + weight);

            log.trace("Added weight {:.4f} to category {} (new value: {:.4f})",
                    weight, category, currentValue + weight);
        }
    }

    public void applyProductTechniquesToVector(Map<String, Double> vector, Product product, double weight) {
        if (product.getTechniques() == null || product.getTechniques().isEmpty()) {
            return;
        }

        for (PaintingTechnique technique : product.getTechniques()) {
            String key = "tech_" + technique.name();
            double currentValue = vector.getOrDefault(key, 0.0);
            vector.put(key, currentValue + weight);

            log.trace("Added weight {:.4f} to technique {} (new value: {:.4f})",
                    weight, technique, currentValue + weight);
        }
    }

    public void applyDerivedFeaturesToVector(Map<String, Double> vector, Product product,
                                             double weight) {
        featureDerivationService.applyPricePreference(vector, product, weight);
        featureDerivationService.applyStylePreference(vector, product, weight);
        featureDerivationService.applyTechnicalComplexityPreference(vector, product, weight);
        featureDerivationService.applyColorIntensityPreference(vector, product, weight);
    }
}