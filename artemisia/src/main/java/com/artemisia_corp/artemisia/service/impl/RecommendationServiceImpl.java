package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.ProductService;
import com.artemisia_corp.artemisia.service.ProductViewService;
import com.artemisia_corp.artemisia.service.RecommendationService;
import com.artemisia_corp.artemisia.utils.recommendation.ViewPreferenceCalculator;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final UserPreferenceRepository userPreferenceRepository;
    private final NotaVentaRepository notaVentaRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductViewService productViewService;
    private final ViewPreferenceCalculator viewPreferenceCalculator;
    private final LogsService logsService;
    private final RecommenderPythonClient recommenderClient;

    @Override
    @Transactional
    public List<ProductResponseDto> getUserRecommendations(Long userId, int limit) {
        try {
            updateUserPreferences(userId);

            // Llamar al servicio Python para recomendaciones mediante el cliente wrapper
            Map[] recommended = recommenderClient.getRecommendations(userId.intValue(), limit);

            if (recommended != null && recommended.length > 0) {
                List<ProductResponseDto> mapped = new ArrayList<>();
                for (Map item : recommended) {
                    Object idObj = item.get("id");
                    if (idObj == null) idObj = item.get("product_id");

                    if (idObj instanceof Number) {
                        Long pid = ((Number) idObj).longValue();
                        try {
                            mapped.add(productService.getProductById(pid));
                        } catch (Exception e) {
                            logsService.error(e.getMessage());
                        }
                    }
                }

                if (!mapped.isEmpty()) {
                    return mapped;
                }
            }

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
            Map<String, Double> userVector = buildUserVector(userId);

            UserPreference preference = userPreferenceRepository.findByUserId(userId)
                    .orElse(UserPreference.builder()
                            .user(userRepository.findById(userId).orElseThrow())
                            .preferenceVector(new HashMap<>())
                            .build());

            preference.setPreferenceVector(userVector);
            userPreferenceRepository.save(preference);

            log.info("Updated preferences for user: {}", userId);

        } catch (Exception e) {
            log.error("Error updating preferences for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public Map<String, Double> buildUserVector(Long userId) {
        Map<String, Double> vector = new HashMap<>();

        // Inicializar con todas las categorías y técnicas
        initializeVector(vector);

        // 1. Preferencias basadas en compras
        addPurchaseBasedPreferences(userId, vector);

        // 2. Preferencias basadas en vistas
        viewPreferenceCalculator.addViewBasedPreferences(userId, vector);

        // Normalizar vector
        normalizeVector(vector);

        return vector;
    }

    private void addPurchaseBasedPreferences(Long userId, Map<String, Double> vector) {
        try {
            // Obtener historial de compras del usuario
            Pageable pageable = Pageable.unpaged();
            Page<NotaVenta> userPurchases = notaVentaRepository.findByBuyerIdAndEstadoVenta(
                    userId, VentaEstado.PAYED, pageable);

            // Ponderar por compras
            for (NotaVenta purchase : userPurchases) {
                List<OrderDetail> orderDetails = orderDetailRepository.findByGroupId(purchase.getId());

                for (OrderDetail detail : orderDetails) {
                    Product product = detail.getProduct();
                    double weight = detail.getQuantity() * detail.getTotal() * 0.0001;

                    // Actualizar vector con categorías del producto
                    if (product.getCategories() != null) {
                        for (PaintingCategory category : product.getCategories()) {
                            String key = "cat_" + category.name();
                            vector.put(key, vector.getOrDefault(key, 0.0) + weight);
                        }
                    }

                    // Actualizar vector con técnicas del producto
                    if (product.getTechniques() != null) {
                        for (PaintingTechnique technique : product.getTechniques()) {
                            String key = "tech_" + technique.name();
                            vector.put(key, vector.getOrDefault(key, 0.0) + weight);
                        }
                    }
                }
            }

            log.debug("Added purchase-based preferences for user: {} with {} purchases",
                    userId, userPurchases.getTotalElements());

        } catch (Exception e) {
            log.error("Error adding purchase-based preferences for user {}: {}", userId, e.getMessage());
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
            // Recopilar todos los datos de usuarios para entrenamiento
            List<Map<String, Object>> trainingData = new ArrayList<>();
            List<User> allUsers = userRepository.findAll();

            for (User user : allUsers) {
                Map<String, Double> userVector = buildUserVector(user.getId());
                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", user.getId());
                userData.put("vector", userVector);

                // Contar compras completadas
                Long purchaseCount = notaVentaRepository.countByBuyerIdAndEstadoVenta(
                        user.getId(), VentaEstado.PAYED);
                userData.put("purchase_count", purchaseCount);

                // Añadir estadísticas de vistas
                Map<String, Object> viewStats = productViewService.getUserViewStatistics(user.getId());
                userData.put("view_statistics", viewStats);

                trainingData.add(userData);
            }

            // Enviar datos al servicio Python para entrenamiento usando el cliente
            String resp = recommenderClient.train(trainingData);
            log.info("Sent training data for {} users to ML service, resp={}", trainingData.size(), resp);

        } catch (Exception e) {
            log.error("Error training recommendation model: {}", e.getMessage());
        }
    }

    private void initializeVector(Map<String, Double> vector) {
        // Inicializar con todas las categorías
        for (PaintingCategory category : PaintingCategory.values()) {
            vector.put("cat_" + category.name(), 0.0);
        }

        // Inicializar con todas las técnicas
        for (PaintingTechnique technique : PaintingTechnique.values()) {
            vector.put("tech_" + technique.name(), 0.0);
        }

        // Características adicionales
        vector.put("price_sensitivity", 0.5);
        vector.put("style_preference", 0.5);
        vector.put("color_intensity", 0.5);
        vector.put("modern_traditional", 0.5);
        vector.put("technical_complexity", 0.5);
    }

    private void normalizeVector(Map<String, Double> vector) {
        double sum = vector.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            for (String key : vector.keySet()) {
                vector.put(key, vector.get(key) / sum);
            }
        }
    }

    private List<ProductResponseDto> getPopularProducts(int limit, long userId) {
        Pageable pageable = Pageable.ofSize(limit);
        return productService.getAvailableProducts(pageable, userId).getContent();
    }
} 