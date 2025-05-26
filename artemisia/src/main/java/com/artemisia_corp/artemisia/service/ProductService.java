package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Long id);
    ProductResponseDto createProduct(ProductRequestDto productDto);
    ProductResponseDto updateProduct(Long id, ProductRequestDto productDto);
    void deleteProduct(Long id);
    void reduceStock(Long productId, Integer quantity);
    List<ProductResponseDto> getAvailableProducts();
    List<ProductResponseDto> getProductsBySeller(Long sellerId);
}