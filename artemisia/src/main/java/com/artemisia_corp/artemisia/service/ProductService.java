package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.product.*;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> listAll();
    List<ProductWithSellerDto> getProductsWithSellers();
    void save(ProductRequestDto company);
    void delete(ProductDeleteDto company);
    void update(ProductUpdateDto company);
}
