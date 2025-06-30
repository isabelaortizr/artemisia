package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
            "AND p.status != 'DELETED'")
    Page<ProductResponseDto> searchWithFilters(
            @Param("category") PaintingCategory category,
            @Param("technique") PaintingTechnique technique,
            @Param("priceMin") Double priceMin,
            @Param("priceMax") Double priceMax,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status IN ('AVAILABLE', 'UNAVAILABLE')")
    Page<Product> findProductsBySellerWithoutDeleted(@Param("sellerId") Long sellerId, Pageable pageable);

    Page<ProductResponseDto> findBySeller_Id(Long sellerId, Pageable pageable);

    Page<ProductResponseDto> findByCategory(PaintingCategory category, Pageable pageable);
    Page<ProductResponseDto> findByTechnique(PaintingTechnique technique, Pageable pageable);
}
