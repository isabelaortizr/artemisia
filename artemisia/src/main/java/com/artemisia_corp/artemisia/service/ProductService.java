package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Long id);
    ProductResponseDto createProduct(ProductRequestDto productDto);
    ProductResponseDto updateProduct(Long id, ProductRequestDto productDto);
    void deleteProduct(Long id);
    void manageStock(ManageProductDto manageProductDto);
    List<ProductResponseDto> getAvailableProducts();
    List<ProductResponseDto> getProductsBySeller(Long sellerId);
    List<ProductResponseDto> searchProducts(ProductSearchDto dto);
    List<ProductResponseDto> getByCategory(String category);
    List<ProductResponseDto> getByTechnique(String technique);
}
