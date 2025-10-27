package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponseDto> getAllProducts(Pageable pageable, long userId);
    ProductResponseDto getProductById(Long id);
    ProductResponseDto createProduct(ProductRequestDto productDto);
    ProductResponseDto updateProduct(Long id, ProductRequestDto productDto);
    void deleteProduct(Long id, String token);
    void manageStock(ManageProductDto manageProductDto);
    Page<ProductResponseDto> getAvailableProducts(Pageable pageable, Long userId);
    Page<ProductResponseDto> getProductsBySeller(Long sellerId, Pageable pageable);
    Page<ProductResponseDto> searchProducts(ProductSearchDto dto, Pageable pageable);
    Page<ProductResponseDto> getByCategory(Long categoryId, Pageable pageable);
    Page<ProductResponseDto> getByTechnique(Long techniqueId, Pageable pageable);
    Page<ProductResponseDto> getProductsBySellerWithoutDeleted(Long sellerId, Pageable pageable);
    Page<ProductResponseDto> getProductsByStatus(Long sellerId, ProductStatus status, Pageable pageable);
}