package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Query mejorado usando DISTINCT para evitar duplicados
    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE p.status != 'DELETED' AND p.seller.id != :user_id")
    Page<Product> findAllProducts(Pageable pageable, @Param("user_id") Long userId);

    @Query("SELECT p FROM Product p WHERE p.id = :p_productId")
    Product findProductById(@Param("p_productId") Long productId);

    // Query para productos disponibles
    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE p.stock > 0 AND p.status = 'AVAILABLE' AND p.seller.id != :user_id")
    Page<Product> findAllAvailableProducts(Pageable pageable, @Param("user_id") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId")
    void reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    void augmentStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Query para búsqueda con filtros usando enums
    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE (:categories IS NULL OR EXISTS (SELECT cat FROM p.categories cat WHERE cat IN :categories)) " +
            "AND (:techniques IS NULL OR EXISTS (SELECT tech FROM p.techniques tech WHERE tech IN :techniques)) " +
            "AND (:priceMin IS NULL OR p.price >= :priceMin) " +
            "AND (:priceMax IS NULL OR p.price <= :priceMax) " +
            "AND p.status = 'AVAILABLE'")
    Page<Product> searchWithFilters(
            @Param("categories") List<PaintingCategory> categories,
            @Param("techniques") List<PaintingTechnique> techniques,
            @Param("priceMin") Double priceMin,
            @Param("priceMax") Double priceMax,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status IN ('AVAILABLE', 'UNAVAILABLE')")
    Page<Product> findProductsBySellerWithoutDeleted(@Param("sellerId") Long sellerId, Pageable pageable);

    // Query para productos por categoría (enum)
    @Query("SELECT DISTINCT p FROM Product p WHERE :category MEMBER OF p.categories")
    Page<Product> findByCategory(@Param("category") PaintingCategory category, Pageable pageable);

    // Query para productos por técnica (enum)
    @Query("SELECT DISTINCT p FROM Product p WHERE :technique MEMBER OF p.techniques")
    Page<Product> findByTechnique(@Param("technique") PaintingTechnique technique, Pageable pageable);

    // Query para contar productos por categoría
    @Query("SELECT COUNT(p) FROM Product p WHERE :category MEMBER OF p.categories AND p.status = 'AVAILABLE'")
    Long countByCategory(@Param("category") PaintingCategory category);

    // Query para contar productos por técnica
    @Query("SELECT COUNT(p) FROM Product p WHERE :technique MEMBER OF p.techniques AND p.status = 'AVAILABLE'")
    Long countByTechnique(@Param("technique") PaintingTechnique technique);

    // Query para productos por vendedor
    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE p.seller.id = :sellerId")
    Page<Product> findBySeller_Id(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status")
    Page<Product> findBySellerIdAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") ProductStatus status,
            Pageable pageable);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto(" +
            "cat, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "JOIN p.categories cat " +
            "WHERE od.group.estadoVenta = 'PAYED' " +
            "GROUP BY cat " +
            "ORDER BY SUM(od.total) DESC")
    List<CategorySalesDto> findTopCategoriesBySales(@Param("limit") int limit);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto(" +
            "tech, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "JOIN p.techniques tech " +
            "WHERE od.group.estadoVenta = 'PAYED' " +
            "GROUP BY tech " +
            "ORDER BY SUM(od.total) DESC")
    List<TechniqueSalesDto> findTopTechniquesBySales(@Param("limit") int limit);
}