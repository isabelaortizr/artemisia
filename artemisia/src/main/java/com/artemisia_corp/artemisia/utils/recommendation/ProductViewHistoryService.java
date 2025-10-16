package com.artemisia_corp.artemisia.utils.recommendation;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.ProductView;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductViewHistoryService {

    @PersistenceContext
    private EntityManager entityManager;

    private final NotaVentaRepository notaVentaRepository;
    private final ProductRepository productRepository;
    private final ProductViewRepository productViewRepository;

    public List<Object[]> getProductViewHistory(Long userId) {
        // Primero intentar obtener datos reales de la base de datos
        List<Object[]> realViewData = getRealViewHistory(userId);
        if (!realViewData.isEmpty()) {
            return realViewData;
        }

        // Si no hay datos reales, usar datos de ejemplo basados en compras
        log.debug("No real view data found for user {}, using fallback data", userId);
        return getFallbackViewHistory(userId);
    }

    private List<Object[]> getRealViewHistory(Long userId) {
        try {
            // Verificar si hay datos en la tabla product_views
            Long viewCount = productViewRepository.countByUserId(userId);
            if (viewCount == null || viewCount == 0) {
                return new ArrayList<>();
            }

            // Consulta para obtener datos reales de product_views
            String query = """
                SELECT pv.product_id, pv.view_count, pv.total_view_duration, 
                       pv.last_viewed_at, pv.first_viewed_at 
                FROM product_views pv 
                WHERE pv.user_id = ?1 
                AND pv.last_viewed_at >= CURRENT_DATE - INTERVAL '30 days'
                ORDER BY pv.view_count DESC, pv.last_viewed_at DESC
                LIMIT 100
            """;

            Query nativeQuery = entityManager.createNativeQuery(query);
            nativeQuery.setParameter(1, userId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = nativeQuery.getResultList();
            return results;

        } catch (Exception e) {
            log.warn("Error getting real view history for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Object[]> getFallbackViewHistory(Long userId) {
        List<Object[]> fallbackData = new ArrayList<>();

        // Simular datos de vista basados en compras recientes
        Pageable pageable = Pageable.unpaged();
        Page<NotaVentaResponseDto> recentPurchases = notaVentaRepository.findAllNotaVentasByBuyer_Id(userId, pageable);

        for (NotaVentaResponseDto purchase : recentPurchases) {
            if ("PAYED".equals(purchase.getEstadoVenta()) && purchase.getDetalles() != null) {
                for (OrderDetailResponseDto detail : purchase.getDetalles()) {
                    // Simular que el usuario también vio estos productos
                    Object[] viewData = {
                            detail.getProductId(),           // product_id
                            getRandomViewCount(),           // view_count (1-10)
                            getRandomViewDuration(),        // total_view_duration (30-300 segundos)
                            LocalDateTime.now().minusDays(getRandomInt(0, 7)), // last_viewed_at
                            LocalDateTime.now().minusDays(getRandomInt(1, 30)) // first_viewed_at
                    };
                    fallbackData.add(viewData);
                }
            }
        }

        // Si no hay compras, simular vistas basadas en productos disponibles
        if (fallbackData.isEmpty()) {
            Page<Product> availableProducts = productRepository.findAllAvailableProducts(PageRequest.of(0, 10));
            for (Product product : availableProducts) {
                Object[] viewData = {
                        product.getId(),
                        getRandomViewCount(),
                        getRandomViewDuration(),
                        LocalDateTime.now().minusDays(getRandomInt(0, 14)),
                        LocalDateTime.now().minusDays(getRandomInt(15, 30))
                };
                fallbackData.add(viewData);
            }
        }

        return fallbackData;
    }

    private int getRandomViewCount() {
        return (int) (Math.random() * 10) + 1;
    }

    private int getRandomViewDuration() {
        return (int) (Math.random() * 270) + 30;
    }

    private int getRandomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }
}