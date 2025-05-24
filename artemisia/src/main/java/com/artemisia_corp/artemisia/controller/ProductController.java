package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.product.*;
import com.artemisia_corp.artemisia.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.listAll());
    }

    @GetMapping("/with_sellers")
    public ResponseEntity<List<ProductWithSellerDto>> getProductsWithSellers() {
        return ResponseEntity.ok(productService.getProductsWithSellers());
    }

    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody ProductRequestDto productDto) {
        productService.save(productDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateProduct(@RequestBody ProductUpdateDto productDto) {
        productService.update(productDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteProduct(@RequestBody Long id) {
        productService.delete(new ProductDeleteDto(id));
        return ResponseEntity.ok().build();
    }
}