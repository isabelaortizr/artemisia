package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;
import com.artemisia_corp.artemisia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
            @RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productDto) {
        ProductResponseDto response = productService.createProduct(productDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id, @RequestBody ProductRequestDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponseDto>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDto>> getProductsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId));
    }

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @RequestBody ManageProductDto manageProductDto) {
        productService.manageStock(manageProductDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@RequestBody ProductSearchDto dto) {
        return ResponseEntity.ok(productService.searchProducts(dto));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    @GetMapping("/technique/{technique}")
    public ResponseEntity<List<ProductResponseDto>> getByTechnique(@PathVariable String technique) {
        return ResponseEntity.ok(productService.getByTechnique(technique));
    }
}
