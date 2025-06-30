package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.*;
import com.artemisia_corp.artemisia.entity.enums.*;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogsService logsService;
    @Autowired
    private ImageService imageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        logsService.info("Fetching all products");
        Page<ProductResponseDto> products = productRepository.findAllProducts(pageable);

        for (ProductResponseDto product : products.getContent()) {
            String image = imageService.getLatestImage(product.getProductId());
            if (image != null && !image.isBlank()) {
                product.setImage(image);
            }
        }

        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        if (id == null || id <= 0) {
            log.error("Product ID must not be null be greater than 0.");
            logsService.error("Product ID must not be null be greater than 0.");
            throw new IllegalArgumentException("Product ID must not be null be greater than 0.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("Product not found with ID: " + id));

        return convertToDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        User seller = this.verifyProduct(productDto);

        Product product = Product.builder()
                .seller(seller)
                .name(productDto.getName())
                .technique(PaintingTechnique.valueOf(productDto.getTechnique()))
                .materials(productDto.getMaterials())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stock(productDto.getStock())
                .status(ProductStatus.valueOf(productDto.getStatus()))
                //.imageUrl(productDto.getImage())
                .category(PaintingCategory.valueOf(productDto.getCategory()))
                .build();

        Product savedProduct = productRepository.save(product);
        if (productDto.getImage() != null && !productDto.getImage().isBlank()) {
            imageService.uploadImage(new ImageUploadDto(savedProduct.getId(), product.getName(), productDto.getImage()));
        }

        logsService.info("Product created with ID: " + savedProduct.getId());
        return convertToDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("Product with ID " + id + " not found."));

        if (productDto.getName() != null && !productDto.getName().isBlank())
            product.setName(productDto.getName());

        if (productDto.getCategory() != null && isValidCategory(productDto.getCategory()))
            product.setCategory(PaintingCategory.valueOf(productDto.getCategory()));

        if (productDto.getTechnique() != null && isValidTechnique(productDto.getTechnique()))
            product.setTechnique(PaintingTechnique.valueOf(productDto.getTechnique()));

        if (productDto.getMaterials() != null && !productDto.getMaterials().isBlank())
            product.setMaterials(productDto.getMaterials());

        if (productDto.getDescription() != null && !productDto.getDescription().isBlank())
            product.setDescription(productDto.getDescription());

        if (productDto.getPrice() != null)
            product.setPrice(productDto.getPrice());

        if (productDto.getStock() != null)
            product.setStock(productDto.getStock());

        if (productDto.getStatus() != null && isValidStatus(productDto.getStatus()))
            product.setStatus(ProductStatus.valueOf(productDto.getStatus()));

        if (productDto.getImage() != null && !productDto.getImage().isBlank())
            product.setImageUrl(productDto.getImage());

        Product updatedProduct = productRepository.save(product);

        return convertToDto(updatedProduct);
    }

    private User verifyProduct(ProductRequestDto productDto) {
        if (productDto == null) {
            log.error("Product data is required.");
            logsService.error("Product data is required.");
            throw new IllegalArgumentException("Product data is required.");
        }
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            log.error("Product name is required.");
            logsService.error("Product name is required.");
            throw new IllegalArgumentException("Product name is required.");
        }
        if (productDto.getCategory() == null || productDto.getCategory().trim().isEmpty()) {
            log.error("Product category is required.");
            logsService.error("Product category is required.");
            throw new IllegalArgumentException("Product category is required.");
        }
        if (productDto.getTechnique() == null || productDto.getTechnique().trim().isEmpty()) {
            log.error("Product category is required.");
            logsService.error("Product category is required.");
            throw new IllegalArgumentException("Product category is required.");
        }

        if (productDto.getStock() == null || productDto.getStock() <= 0) {
            log.error("Product stock is required.");
            logsService.error("Product stock is required.");
            throw new IllegalArgumentException("Product stock is required.");
        }

        if (productDto.getPrice() == null || productDto.getPrice() <= 0) {
            log.error("Product price must be greater than 0.");
            logsService.error("Product price must be greater than 0.");
            throw new IllegalArgumentException("Product price must be greater than 0.");
        }

        return userRepository.findById(productDto.getSellerId())
                .orElseThrow(() -> {
                    log.error("User not found with ID: " + productDto.getSellerId());
                    logsService.error("User not found with ID: " + productDto.getSellerId());
                    return new NotDataFoundException("User not found");
                });
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            log.error("Product not found with ID: " + id);
            logsService.error("Product not found with ID: " + id);
            throw new NotDataFoundException("Product not found");
        }

        Product product = productRepository.getReferenceById(id);
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        logsService.info("Product deleted with ID: " + id);
    }

    @Override
    @Transactional
    public void manageStock(ManageProductDto manageProductDto) {
        Long productId = manageProductDto.getProductId();
        int quantity = manageProductDto.getQuantity();
        boolean reduceStock = manageProductDto.isReduceStock();

        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + productId);
                    return new NotDataFoundException("Product not found");
                });

        if (reduceStock) {
            if (product.getStock() < quantity ||
                    product.getStock() - quantity < 0) {
                logsService.error("Insufficient stock for product ID: " + productId);
                throw new NotDataFoundException("Insufficient stock");
            }
            product.setStock(product.getStock() - quantity);
            logsService.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
        } else {
            product.setStock(product.getStock() + quantity);
            logsService.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
        }

        if (product.getStock() - quantity <= 0) {
            product.setStatus(ProductStatus.UNAVAILABLE);
            logsService.info("Product status updated to UNAVAILABLE for ID: " + productId);
        }
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAvailableProducts(Pageable pageable) {
        logsService.info("Fetching all available products (stock > 0)");
        try {
            Page<ProductResponseDto> products = productRepository.findAllAvailableProducts(pageable);

            for (ProductResponseDto product : products.getContent()) {
                String image = imageService.getLatestImage(product.getProductId());
                if (image != null && !image.isBlank()) {
                    product.setImage(image);
                }
            }

            return products;
        } catch (Exception e) {
            logsService.error("Error fetching available products: " + e.getMessage());
            throw new NotDataFoundException("Error fetching available products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsBySeller(Long sellerId, Pageable pageable) {
        logsService.info("Fetching products for seller ID: " + sellerId);

        if (!userRepository.existsById(sellerId)) {
            logsService.error("Seller not found with ID: " + sellerId);
            throw new NotDataFoundException("Seller not found with ID: " + sellerId);
        }
        try {
            Page<ProductResponseDto> products = productRepository.findBySeller_Id(sellerId, pageable);

            for (ProductResponseDto product : products.getContent()) {
                String image = imageService.getLatestImage(product.getProductId());
                if (image != null && !image.isBlank()) {
                    product.setImage(image);
                }
            }
            return products;
        } catch (Exception e) {
            logsService.error("Error fetching products for seller: " + e.getMessage());
            throw new NotDataFoundException("Error fetching products for seller", e);
        }

//        try {
//            return productRepository.findBySeller_Id(sellerId, pageable);
//        } catch (Exception e) {
//            logsService.error("Error fetching products for seller: " + e.getMessage());
//            throw new NotDataFoundException("Error fetching products for seller", e);
//        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(ProductSearchDto dto, Pageable pageable) {
        PaintingCategory category = null;
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            if (isValidCategory(dto.getCategory())) {
                category = PaintingCategory.valueOf(dto.getCategory());
            } else {
                throw new IllegalArgumentException("Invalid category provided: " + dto.getCategory());
            }
        }

        PaintingTechnique technique = null;
        if (dto.getTechnique() != null && !dto.getTechnique().isBlank()) {
            if (isValidTechnique(dto.getTechnique())) {
                technique = PaintingTechnique.valueOf(dto.getTechnique());
            } else {
                throw new IllegalArgumentException("Invalid technique provided: " + dto.getTechnique());
            }
        }

        // Call repository method with validated inputs
        return productRepository.searchWithFilters(
                category,
                technique,
                dto.getPriceMin(),
                dto.getPriceMax(),
                pageable
        );
    }

    @Override
    public Page<ProductResponseDto> getByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            log.error("Product category is required.");
            logsService.error("Product category is required.");
            throw new IllegalArgumentException("Product category is required.");
        }
        logsService.info("Fetching products by category: " + category);
        return productRepository.findByCategory(PaintingCategory.valueOf(category), pageable);
    }

    @Override
    public Page<ProductResponseDto> getByTechnique(String technique, Pageable pageable) {
        if (technique == null || technique.trim().isEmpty()) {
            log.error("Product category is required.");
            logsService.error("Product category is required.");
            throw new IllegalArgumentException("Product category is required.");
        }
        logsService.info("Fetching products by technique: " + technique);
        return productRepository.findByTechnique(PaintingTechnique.valueOf(technique), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsBySellerWithoutDeleted(Long sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findProductsBySellerWithoutDeleted(sellerId, pageable);
        return products.map(this::convertToDto);
    }

    private ProductResponseDto convertToDto(Product product) {
        String image = imageService.getLatestImage(product.getId());

        return ProductResponseDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .technique(product.getTechnique().name())
                .materials(product.getMaterials())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .image(image != null && !image.isBlank() ? image : product.getImageUrl() != null ? product.getImageUrl() : "")
                .category(product.getCategory().name())
                .sellerId(product.getSeller().getId())
                .build();
    }

    private boolean isValidCategory(String category) {
        return Arrays.stream(PaintingCategory.values())
                .map(Enum::name)
                .collect(Collectors.toSet())
                .contains(category);
    }

    private boolean isValidTechnique(String technique) {
        return Arrays.stream(PaintingTechnique.values())
                .map(Enum::name)
                .collect(Collectors.toSet())
                .contains(technique);
    }

    private boolean isValidStatus(String status) {
        return Arrays.stream(ProductStatus.values())
                .map(Enum::name)
                .collect(Collectors.toSet())
                .contains(status);
    }

}