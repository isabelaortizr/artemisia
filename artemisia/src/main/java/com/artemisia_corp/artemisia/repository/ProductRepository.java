package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p) " +
            "FROM Product p where p.stock > 1")
    List<ProductResponseDto> findAllProducts();

    @Query("select new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p.name, p.description, p.price) " +
            "FROM Product p")
    List<ProductResponseDto> findAllProductsReduced();

    @Query("select new com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto(p.name, p.description, p.price) " +
            "FROM Product p where p.name = :name")
    ProductResponseDto findProductByName(@Param("name") String name);

}
