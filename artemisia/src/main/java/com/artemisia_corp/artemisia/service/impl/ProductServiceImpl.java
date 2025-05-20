package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.product.ProductDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductUpdateDto;
import com.artemisia_corp.artemisia.repository.CompanyRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private ProductRepository productRepository;
    private CompanyRepository companyRepository;

    @Override
    public List<ProductResponseDto> listAll() {
        return productRepository.findAllProductsReduced();
    }

    @Override
    public ProductResponseDto getByName(String productName) {
        return productRepository.findProductByName(productName);
    }

    @Override
    public void save(ProductRequestDto product) {
        Company company = companyRepository.findById(product.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + product.getCompanyId()));
        this.productRepository.save(Product.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .company(company)
                .build());
    }

    @Override
    public void delete(ProductDeleteDto product) {
        productRepository.deleteById(product.getId());
    }

    @Override
    public void update(ProductUpdateDto product) {
        Product existingProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new RuntimeException("Company with ID " + product.getProductId() + " not found"));

        Company company = companyRepository.findById(product.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + product.getCompanyId()));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setCompany(company);

        productRepository.save(existingProduct);
    }
}
