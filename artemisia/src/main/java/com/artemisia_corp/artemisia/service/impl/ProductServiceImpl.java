package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.*;
import com.artemisia_corp.artemisia.entity.enums.*;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogsService logsService;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public List<ProductResponseDto> getAllProducts() {
        logsService.info("Fetching all products");
        return productRepository.findAllProducts();
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        logsService.info("Fetching product with ID: " + id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + id);
                    throw new RuntimeException("Product not found");
                });
        return convertToDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        User seller = userRepository.findById(productDto.getSellerId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + productDto.getSellerId());
                    throw new RuntimeException("User not found");
                });

        if (!seller.getRole().equals(UserRole.SELLER)) {
            logsService.error("User is not a seller: " + productDto.getSellerId());
            throw new RuntimeException("Only sellers can create products");
        }

        Product product = Product.builder()
                .seller(seller)
                .name(productDto.getName())
                .technique(PaintingTechnique.valueOf(productDto.getTechnique()))
                .materials(productDto.getMaterials())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stock(productDto.getStock())
                .status(ProductStatus.valueOf(productDto.getStatus()))
                .imageUrl(productDto.getImage())
                .category(PaintingCategory.valueOf(productDto.getCategory()))
                .build();

        Product savedProduct = productRepository.save(product);
        logsService.info("Product created with ID: " + savedProduct.getProductId());
        return convertToDto(savedProduct);
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + id);
                    throw new RuntimeException("Product not found");
                });

        User seller = userRepository.findById(productDto.getSellerId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + productDto.getSellerId());
                    throw new RuntimeException("User not found");
                });

        if (!seller.getRole().equals(UserRole.SELLER)) {
            logsService.error("User is not a seller: " + productDto.getSellerId());
            throw new RuntimeException("Only sellers can update products");
        }

        product.setSeller(seller);
        product.setName(productDto.getName());
        product.setTechnique(PaintingTechnique.valueOf(productDto.getTechnique()));
        product.setMaterials(productDto.getMaterials());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setStatus(ProductStatus.valueOf(productDto.getStatus()));
        product.setImageUrl(productDto.getImage());
        product.setCategory(PaintingCategory.valueOf(productDto.getCategory()));

        Product updatedProduct = productRepository.save(product);
        logsService.info("Product updated with ID: " + updatedProduct.getProductId());
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            logsService.error("Product not found with ID: " + id);
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
        logsService.info("Product deleted with ID: " + id);
    }

    @Override
    @Transactional
    public void manageStock(ManageProductDto manageProductDto) {
        Long productId = manageProductDto.getProductId();
        int quantity = manageProductDto.getQuantity();
        boolean reduceStock = manageProductDto.isReduceStock();

        Product product = productRepository.findProductByProductId(productId)
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + productId);
                    throw new RuntimeException("Product not found");
                });

        if (product.getStock() < quantity ||
                product.getStock() - quantity < 0) {
            logsService.error("Insufficient stock for product ID: " + productId);
            throw new RuntimeException("Insufficient stock");
        }

        if (reduceStock) {
            productRepository.reduceStock(productId, quantity);
            logsService.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
        } else {
            productRepository.augmentStock(productId, quantity);
            logsService.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
        }

        if (product.getStock() - quantity == 0) {
            product.setStatus(ProductStatus.UNAVAILABLE);
            productRepository.save(product);
            logsService.info("Product status updated to UNAVAILABLE for ID: " + productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAvailableProducts() {
        logsService.info("Fetching all available products (stock > 0)");
        try {
            return productRepository.findAllAvailableProducts();
        } catch (Exception e) {
            logsService.error("Error fetching available products: " + e.getMessage());
            throw new RuntimeException("Error fetching available products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsBySeller(Long sellerId) {
        logsService.info("Fetching products for seller ID: " + sellerId);

        // Verificar que el vendedor existe
        if (!userRepository.existsById(sellerId)) {
            logsService.error("Seller not found with ID: " + sellerId);
            throw new RuntimeException("Seller not found with ID: " + sellerId);
        }

        try {
            return productRepository.findBySeller_Id(sellerId);
        } catch (Exception e) {
            logsService.error("Error fetching products for seller: " + e.getMessage());
            throw new RuntimeException("Error fetching products for seller", e);
        }
    }

    private ProductResponseDto convertToDto(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .technique(product.getTechnique().name())
                .materials(product.getMaterials())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .image(product.getImageUrl())
                .category(product.getCategory().name())
                .sellerId(product.getSeller().getId())
                .build();
    }

    @Override
    public List<ProductResponseDto> searchProducts(ProductSearchDto dto) {
        logsService.info("Searching products with filters");
        return productRepository.searchWithFilters(dto);
    }

    @Override
    public List<ProductResponseDto> getByCategory(String category) {
        logsService.info("Fetching products by category: " + category);
        return productRepository.findByCategory(PaintingCategory.valueOf(category));
    }

    @Override
    public List<ProductResponseDto> getByTechnique(String technique) {
        logsService.info("Fetching products by technique: " + technique);
        return productRepository.findByTechnique(PaintingTechnique.valueOf(technique));
    }
}