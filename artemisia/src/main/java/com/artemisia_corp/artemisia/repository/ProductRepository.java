package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p)" +
            "FROM Product p")
    List<ProductResponseDto> findAllProducts();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p)" +
            "FROM Product p WHERE p.stock > 0 AND p.status = 'AVAILABLE'")
    List<ProductResponseDto> findAllAvailableProducts();

    @Query("SELECT p FROM Product p WHERE p.seller = :sellerId")
    List<Product> findProduct(@Param("sellerId") Long sellerId);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.productId = :productId")
    void reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.productId = :productId")
    void augmentStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    List<ProductResponseDto> findBySeller_Id(Long sellerId);
}