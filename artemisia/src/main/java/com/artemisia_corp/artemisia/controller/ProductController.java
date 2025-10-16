package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductSearchDto;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.service.ProductService;
import com.artemisia_corp.artemisia.service.ProductViewService;
import com.artemisia_corp.artemisia.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@Tag(name = "Product Management", description = "Endpoints for managing products")
public class ProductController {

    @Autowired
    @Lazy
    private ProductService productService;

    @Autowired
    @Lazy
    private ProductViewService productViewService;

    @Autowired
    @Lazy
    private JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all products", description = "Returns paginated list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @Operation(summary = "Get product by ID", description = "Returns a single product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // El tracking se hace automáticamente en el ProductServiceImpl
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Manually track product view", description = "Explicitly track a product view (useful for frontend tracking)")
    @PostMapping("/{id}/track-view")
    public ResponseEntity<Void> trackProductView(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "duration", required = false) Integer durationSeconds) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        if (durationSeconds != null) {
            productViewService.trackProductViewWithDuration(userId, id, durationSeconds);
        } else {
            productViewService.trackProductView(userId, id);
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Create a new product", description = "Creates a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestBody ProductRequestDto productDto,
            @RequestHeader("Authorization") String token) {
        productDto.setSellerId(jwtTokenProvider.getUserIdFromToken(token));
        ProductResponseDto response = productService.createProduct(productDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a product", description = "Updates an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDto productDto,
            @RequestHeader("Authorization") String token) {
        productDto.setSellerId(jwtTokenProvider.getUserIdFromToken(token));
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        productService.deleteProduct(id, token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get available products", description = "Returns paginated list of available products (in stock)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/available")
    public ResponseEntity<Page<ProductResponseDto>> getAvailableProducts(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getAvailableProducts(pageable));
    }

    @Operation(summary = "Get products by seller", description = "Returns paginated list of products for a specific seller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Seller not found")
    })
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

    @Operation(summary = "Reduce product stock", description = "Reduces the stock quantity of a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reduced successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity")
    })
    @PutMapping("/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @RequestBody ManageProductDto manageProductDto) {
        productService.manageStock(manageProductDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Search products", description = "Searches products based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
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

    @Operation(summary = "Get products by category", description = "Returns paginated list of products filtered by category ID")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getByCategory(categoryId, pageable));
    }

    @Operation(summary = "Get products by technique", description = "Returns paginated list of products filtered by technique ID")
    @GetMapping("/technique/{techniqueId}")
    public ResponseEntity<Page<ProductResponseDto>> getByTechnique(
            @PathVariable Long techniqueId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(productService.getByTechnique(techniqueId, pageable));
    }

    @GetMapping("/seller/{sellerId}/without-deleted")
    public ResponseEntity<Page<ProductResponseDto>> getProductsBySellerWithoutDeleted(
            @PathVariable Long sellerId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ProductResponseDto> products = productService.getProductsBySellerWithoutDeleted(sellerId, pageable);
        return ResponseEntity.ok(products);
    }
}
