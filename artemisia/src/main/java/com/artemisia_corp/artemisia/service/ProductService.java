package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyUpdateDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductUpdateDto;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> listAll();
    ProductResponseDto getByName(String productName);
    void save(ProductRequestDto company);
    void delete(ProductDeleteDto company);
    void update(ProductUpdateDto company);
}
