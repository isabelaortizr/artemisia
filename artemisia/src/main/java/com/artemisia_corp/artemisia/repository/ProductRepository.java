package com.artemisia_corp.artemisia.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto;
import com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
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
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p)" +
            "FROM Product p WHERE p.status != 'DELETED' OR p.status != 'UNAVAILABLE'")
    Page<ProductResponseDto> findAllProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :p_productId")
    Optional<Product> findProductById(@Param("p_productId") Long productId);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p)" +
            "FROM Product p WHERE p.stock > 0 AND p.status = 'AVAILABLE'")
    Page<ProductResponseDto> findAllAvailableProducts(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId")
    void reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    void augmentStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p) " +
            "FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) " +
            "AND (:technique IS NULL OR p.technique = :technique) " +
            "AND (:priceMin IS NULL OR p.price >= :priceMin) " +
            "AND (:priceMax IS NULL OR p.price <= :priceMax) " +
            "AND p.status = 'AVAILABLE'")
    Page<ProductResponseDto> searchWithFilters(
            @Param("category") PaintingCategory category,
            @Param("technique") PaintingTechnique technique,
            @Param("priceMin") Double priceMin,
            @Param("priceMax") Double priceMax,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status IN ('AVAILABLE', 'UNAVAILABLE')")
    Page<Product> findProductsBySellerWithoutDeleted(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.CategorySalesDto(" +
            "p.category, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "WHERE od.group.estadoVenta = 'PAYED' " +
            "GROUP BY p.category " +
            "ORDER BY SUM(od.total) DESC " +
            "LIMIT :limit")
    List<CategorySalesDto> findTopCategoriesBySales(@Param("limit") int limit);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.admin_dashboard.TechniqueSalesDto(" +
            "p.technique, COUNT(od), SUM(od.total)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "WHERE od.group.estadoVenta = 'PAYED' " +
            "GROUP BY p.technique " +
            "ORDER BY SUM(od.total) DESC " +
            "LIMIT :limit")
    List<TechniqueSalesDto> findTopTechniquesBySales(@Param("limit") int limit);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status")
    Page<Product> findBySellerIdAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") ProductStatus status,
            Pageable pageable);

    Page<ProductResponseDto> findBySeller_Id(Long sellerId, Pageable pageable);

    Page<ProductResponseDto> findByCategory(PaintingCategory category, Pageable pageable);
    Page<ProductResponseDto> findByTechnique(PaintingTechnique technique, Pageable pageable);
}
