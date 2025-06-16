package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
            "FROM Product p")
    List<ProductResponseDto> findAllProducts();

    @Query("SELECT p FROM Product p WHERE p.productId = :p_productId")
    Optional<Product> findProductByProductId(@Param("p_productId") Long productId);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p)" +
            "FROM Product p WHERE p.stock > 0 AND p.status = 'AVAILABLE'")
    List<ProductResponseDto> findAllAvailableProducts();

    @Query("SELECT p FROM Product p WHERE p.seller = :sellerId")
    List<Product> findProductBySeller(@Param("sellerId") Long sellerId);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.productId = :productId")
    void reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.productId = :productId")
    void augmentStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p) " +
            "FROM Product p WHERE (:#{#dto.category} IS NULL " +
            "OR p.category = :#{#dto.category}) AND (:#{#dto.technique} IS NULL " +
            "OR p.technique = :#{#dto.technique}) AND (:#{#dto.priceMin} IS NULL " +
            "OR p.price >= :#{#dto.priceMin}) AND (:#{#dto.priceMax} IS NULL " +
            "OR p.price <= :#{#dto.priceMax})")
    List<ProductResponseDto> searchWithFilters(@Param("dto") ProductSearchDto dto);

    List<ProductResponseDto> findBySeller_Id(Long sellerId);

    List<ProductResponseDto> findByCategory(PaintingCategory category);
    List<ProductResponseDto> findByTechnique(PaintingTechnique technique);
}
