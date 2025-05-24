package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductWithSellerDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductWithSellerDto(" +
            "p, u.id) " +
            "FROM Product p JOIN p.seller u WHERE p.stock > 0")
    List<ProductWithSellerDto> findProductsWithSeller();

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p) " +
            "FROM Product p")
    List<ProductResponseDto> findProducts();
}
