package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.service.ProductService;
import com.artemisia_corp.artemisia.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
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
    public ResponseEntity<Page<ProductResponseDto>> getAvailableProducts(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getAvailableProducts(pageable));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageable));
    }

    @PutMapping("/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @RequestBody ManageProductDto manageProductDto) {
        productService.manageStock(manageProductDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestBody ProductSearchDto dto,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = DateUtils.FORMAT_ISO_8601_SHORT) Date from,
            @RequestParam(value = "to" , required = false) @DateTimeFormat(pattern = DateUtils.FORMAT_ISO_8601_SHORT) Date to) {

        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");


        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
            return ResponseEntity.ok(productService.searchProducts(dto, pageable));
        } catch (OperationException e) {
            log.error("Error al listar el empresas. Causa:{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error al listar el empresas", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponseDto>> getByCategory(
            @PathVariable String category,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getByCategory(category, pageable));
    }

    @GetMapping("/technique/{technique}")
    public ResponseEntity<Page<ProductResponseDto>> getByTechnique(
            @PathVariable String technique,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getByTechnique(technique, pageable));
    }
}
