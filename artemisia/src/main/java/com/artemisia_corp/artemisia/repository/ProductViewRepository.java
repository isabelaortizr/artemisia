package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    // Buscar vista específica de usuario-producto
    Optional<ProductView> findByUserIdAndProductId(Long userId, Long productId);

    // Obtener todas las vistas de un usuario ordenadas por fecha
    List<ProductView> findByUserIdOrderByLastViewedAtDesc(Long userId);

    // Obtener vistas de un usuario con paginación
    Page<ProductView> findByUserIdOrderByLastViewedAtDesc(Long userId, Pageable pageable);

    // Obtener productos más vistos por un usuario
    @Query("SELECT pv FROM ProductView pv WHERE pv.user.id = :userId ORDER BY pv.viewCount DESC, pv.lastViewedAt DESC")
    List<ProductView> findTopViewedByUserId(@Param("userId") Long userId, Pageable pageable);

    // Obtener vistas recientes (últimos N días)
    @Query("SELECT pv FROM ProductView pv WHERE pv.user.id = :userId AND pv.lastViewedAt >= :sinceDate ORDER BY pv.lastViewedAt DESC")
    List<ProductView> findRecentViewsByUser(@Param("userId") Long userId, @Param("sinceDate") LocalDateTime sinceDate);

    // Contar vistas totales de un usuario
    Long countByUserId(Long userId);

    // Obtener duración total de visualización de un usuario
    @Query("SELECT COALESCE(SUM(pv.totalViewDuration), 0) FROM ProductView pv WHERE pv.user.id = :userId")
    Long getTotalViewDurationByUser(@Param("userId") Long userId);

    // Limpiar vistas antiguas
    @Modifying
    @Query("DELETE FROM ProductView pv WHERE pv.lastViewedAt < :cutoffDate")
    int deleteOldViews(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Obtener productos similares basados en vistas de otros usuarios
    @Query("SELECT pv2.product.id FROM ProductView pv1 " +
            "JOIN ProductView pv2 ON pv1.user.id = pv2.user.id AND pv1.product.id != pv2.product.id " +
            "WHERE pv1.product.id = :productId AND pv1.user.id = :userId " +
            "ORDER BY pv2.viewCount DESC")
    List<Long> findSimilarViewedProducts(@Param("userId") Long userId, @Param("productId") Long productId, Pageable pageable);

    // Verificar si existe una vista para usuario-producto
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserId(Long userId);

    // Obtener estadísticas de vistas por usuario
    @Query("SELECT COUNT(pv), COALESCE(SUM(pv.viewCount), 0), COALESCE(SUM(pv.totalViewDuration), 0) " +
            "FROM ProductView pv WHERE pv.user.id = :userId")
    Object[] getUserViewStatistics(@Param("userId") Long userId);
}