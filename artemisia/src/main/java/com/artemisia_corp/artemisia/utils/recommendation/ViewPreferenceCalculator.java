package com.artemisia_corp.artemisia.utils.recommendation;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewPreferenceCalculator {

    private final ProductViewHistoryService productViewHistoryService;
    private final ViewWeightCalculator viewWeightCalculator;
    private final ViewPreferenceApplier viewPreferenceApplier;
    private final ProductRepository productRepository;

    public void addViewBasedPreferences(Long userId, Map<String, Double> vector) {
        try {
            log.debug("Starting view-based preferences calculation for user: {}", userId);

            // 1. Obtener el historial de vistas del usuario
            List<Object[]> viewHistory = productViewHistoryService.getProductViewHistory(userId);

            if (viewHistory.isEmpty()) {
                log.debug("No view history found for user: {}", userId);
                return;
            }

            log.debug("Found {} product views for user: {}", viewHistory.size(), userId);

            // 2. Calcular pesos basados en el comportamiento de visualización
            Map<Long, Double> productViewWeights = viewWeightCalculator.calculateViewWeights(viewHistory);

            // 3. Obtener información detallada de los productos vistos
            Map<Long, Product> viewedProducts = getViewedProductsDetails(new ArrayList<>(productViewWeights.keySet()));

            // 4. Aplicar las preferencias basadas en vistas al vector
            applyViewPreferencesToVector(vector, productViewWeights, viewedProducts);

            log.info("Successfully added view-based preferences for user: {} with {} products",
                    userId, productViewWeights.size());

        } catch (Exception e) {
            log.error("Error in addViewBasedPreferences for user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void applyViewPreferencesToVector(Map<String, Double> vector,
                                              Map<Long, Double> viewWeights,
                                              Map<Long, Product> viewedProducts) {
        double totalViewWeight = viewWeights.values().stream().mapToDouble(Double::doubleValue).sum();

        if (totalViewWeight <= 0) {
            return;
        }

        // Factor de influencia de las vistas vs compras (ajustable)
        double viewInfluenceFactor = 0.3; // 30% de influencia

        for (Map.Entry<Long, Double> entry : viewWeights.entrySet()) {
            Long productId = entry.getKey();
            Double viewWeight = entry.getValue();
            Product product = viewedProducts.get(productId);

            if (product == null) {
                continue;
            }

            // Peso normalizado y ajustado por el factor de influencia
            double adjustedWeight = (viewWeight / totalViewWeight) * viewInfluenceFactor;

            // Aplicar categorías del producto al vector
            viewPreferenceApplier.applyProductCategoriesToVector(vector, product, adjustedWeight);

            // Aplicar técnicas del producto al vector
            viewPreferenceApplier.applyProductTechniquesToVector(vector, product, adjustedWeight);

            // Aplicar características derivadas del producto
            viewPreferenceApplier.applyDerivedFeaturesToVector(vector, product, adjustedWeight);
        }

        log.debug("Applied view preferences with influence factor: {}", viewInfluenceFactor);
    }

    private Map<Long, Product> getViewedProductsDetails(List<Long> productIds) {
        Map<Long, Product> products = new HashMap<>();

        if (productIds.isEmpty()) {
            return products;
        }

        try {
            // Obtener todos los productos de una sola consulta
            List<Product> productList = productRepository.findAllById(productIds);

            for (Product product : productList) {
                products.put(product.getId(), product);
            }

            log.debug("Retrieved details for {} viewed products", products.size());

        } catch (Exception e) {
            log.error("Error getting product details: {}", e.getMessage());
        }

        return products;
    }
}