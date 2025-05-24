package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.Users;
import com.artemisia_corp.artemisia.entity.dto.product.*;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.UsersRepository;
import com.artemisia_corp.artemisia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public List<ProductResponseDto> listAll() {
        return productRepository.findProducts();
    }

    @Override
    public List<ProductWithSellerDto> getProductsWithSellers() {
        return productRepository.findProductsWithSeller();
    }

    @Override
    public void save(ProductRequestDto productDto) {
        Users seller = usersRepository.findById(productDto.getSellerId())
                .orElseThrow(() -> {

                    return new RuntimeException("Vendedor no encontrado con ID: " + productDto.getSellerId())
                });

        Product product = Product.builder()
                .name(productDto.getName())
                .technique(PaintingTechnique.valueOf(productDto.getTechnique()))
                .materials(productDto.getMaterials())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stock(productDto.getStock() != null ? productDto.getStock() : 0)
                .status(ProductStatus.valueOf(productDto.getStatus() != null ? productDto.getStatus() : "AVAILABLE"))
                .image(productDto.getImage())
                .category(PaintingCategory.valueOf(productDto.getCategory()))
                .build();
        product.setSeller(seller);
        productRepository.save(product);
    }

    @Override
    public void delete(ProductDeleteDto productDto) {
        if (!productRepository.existsById(productDto.getProductId())) {
            throw new RuntimeException("Producto no encontrado con ID: " + productDto.getProductId());
        }
        productRepository.deleteById(productDto.getProductId());
    }

    @Override
    public void update(ProductUpdateDto productDto) {
        Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productDto.getProductId()));

        if (productDto.getName() != null) product.setName(productDto.getName());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
        if (productDto.getStock() != null) product.setStock(productDto.getStock());
        if (productDto.getImage() != null) product.setImage(productDto.getImage());
        if (productDto.getMaterials() != null) product.setMaterials(productDto.getMaterials());

        if (productDto.getTechnique() != null) product.setTechnique(PaintingTechnique.valueOf(productDto.getTechnique()));
        if (productDto.getStatus() != null) product.setStatus(ProductStatus.valueOf(productDto.getStatus()));
        if (productDto.getCategory() != null) product.setCategory(PaintingCategory.valueOf(productDto.getCategory()));


        if (productDto.getSellerId() != null) {
            Users seller = usersRepository.findById(productDto.getSellerId())
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + productDto.getSellerId()));
            product.setSeller(seller);
        }

        productRepository.save(product);
    }
}