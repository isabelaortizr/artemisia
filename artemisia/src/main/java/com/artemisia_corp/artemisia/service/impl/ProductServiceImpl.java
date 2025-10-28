package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.*;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LogsService logsService;
    private final ImageService imageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable, long userId) {
        logsService.info("Fetching all products");
        log.info("user id: " + userId);
        Page<Product> products = productRepository.findAllProducts(pageable, userId);
        products.forEach(product ->
            log.info(String.valueOf(product.getSeller().getId()))
        );
        // Convertir a DTO y cargar imágenes
        List<ProductResponseDto> productDtos = products.getContent().stream()
                .map(this::convertToDtoWithImage)
                .toList();

        return new PageImpl<>(productDtos, pageable, products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        if (id == null || id <= 0) {
            log.error("Product ID must not be null be greater than 0.");
            logsService.error("Product ID must not be null be greater than 0.");
            throw new IllegalArgumentException("Product ID must not be null be greater than 0.");
        }

        Product product = productRepository.findProductById(id);
        if (product == null) {
            throw new NotDataFoundException("Product not found with ID: " + id);
        }

        return convertToDtoWithImage(product);
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        User seller = this.verifyProduct(productDto);

        // Convertir directamente los enums del DTO
        Set<PaintingCategory> categories = productDto.getCategories() != null ?
                new HashSet<>(productDto.getCategories()) : new HashSet<>();

        Set<PaintingTechnique> techniques = productDto.getTechniques() != null ?
                new HashSet<>(productDto.getTechniques()) : new HashSet<>();

        Product product = Product.builder()
                .seller(seller)
                .name(productDto.getName())
                .materials(productDto.getMaterials())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stock(productDto.getStock())
                .status(ProductStatus.valueOf(productDto.getStatus()))
                .categories(categories)
                .techniques(techniques)
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
        Product product = productRepository.findProductById(id);
        if (product == null) {
            throw new NotDataFoundException("Product with ID " + id + " not found.");
        }

        if (productDto.getName() != null && !productDto.getName().isBlank())
            product.setName(productDto.getName());

        if (productDto.getMaterials() != null && !productDto.getMaterials().isBlank())
            product.setMaterials(productDto.getMaterials());

        if (productDto.getDescription() != null && !productDto.getDescription().isBlank())
            product.setDescription(productDto.getDescription());

        if (productDto.getPrice() != null)
            product.setPrice(productDto.getPrice());

        if (productDto.getStock() != null)
            product.setStock(productDto.getStock());

        if (productDto.getStatus() != null)
            product.setStatus(ProductStatus.valueOf(productDto.getStatus()));

        if (productDto.getCategories() != null) {
            product.setCategories(new HashSet<>(productDto.getCategories()));
        }

        if (productDto.getTechniques() != null) {
            product.setTechniques(new HashSet<>(productDto.getTechniques()));
        }

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
        if (productDto.getCategories() == null || productDto.getCategories().isEmpty()) {
            log.error("At least one category is required.");
            logsService.error("At least one category is required.");
            throw new IllegalArgumentException("At least one category is required.");
        }
        if (productDto.getTechniques() == null || productDto.getTechniques().isEmpty()) {
            log.error("At least one technique is required.");
            logsService.error("At least one technique is required.");
            throw new IllegalArgumentException("At least one technique is required.");
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
    public void deleteProduct(Long id, String token) {
        Product product = productRepository.findProductById(id);
        if (product == null) {
            log.error("Product not found with ID: " + id);
            logsService.error("Product not found with ID: " + id);
            throw new NotDataFoundException("Product not found");
        }

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

        Product product = productRepository.findProductById(productId);
        if (product == null) {
            logsService.error("Product not found with ID: " + productId);
            throw new NotDataFoundException("Product not found");
        }

        if (reduceStock) {
            if (product.getStock() < quantity || product.getStock() - quantity < 0) {
                logsService.error("Insufficient stock for product ID: " + productId);
                throw new NotDataFoundException("Insufficient stock");
            }
            product.setStock(product.getStock() - quantity);
            logsService.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
            if (product.getStock() == 0) {
                product.setStatus(ProductStatus.UNAVAILABLE);
                logsService.info("Product status updated to UNAVAILABLE for ID: " + productId);
            }
        } else {
            product.setStock(product.getStock() + quantity);
            logsService.info("Stock increased for product ID: " + productId + " by quantity: " + quantity);
            if (product.getStatus() == ProductStatus.UNAVAILABLE && product.getStock() > 0) {
                product.setStatus(ProductStatus.AVAILABLE);
                logsService.info("Product status updated to AVAILABLE for ID: " + productId);
            }
        }
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAvailableProducts(Pageable pageable, Long userId) {
        logsService.info("Fetching all available products (stock > 0)");
        try {
            Page<Product> products = productRepository.findAllAvailableProducts(pageable, userId);

            List<ProductResponseDto> productDtos = products.getContent().stream()
                    .map(this::convertToDtoWithImage)
                    .toList();

            return new PageImpl<>(productDtos, pageable, products.getTotalElements());
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
            Page<Product> products = productRepository.findBySeller_Id(sellerId, pageable);

            List<ProductResponseDto> productDtos = products.getContent().stream()
                    .map(this::convertToDtoWithImage)
                    .toList();

            return new PageImpl<>(productDtos, pageable, products.getTotalElements());
        } catch (Exception e) {
            logsService.error("Error fetching products for seller: " + e.getMessage());
            throw new NotDataFoundException("Error fetching products for seller", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(ProductSearchDto dto, Pageable pageable) {
        List<PaintingCategory> categories = dto.getCategories();
        List<PaintingTechnique> techniques = dto.getTechniques();

        Page<Product> products = productRepository.searchWithFilters(
                categories,
                techniques,
                dto.getPriceMin(),
                dto.getPriceMax(),
                pageable
        );

        List<ProductResponseDto> productDtos = products.getContent().stream()
                .map(this::convertToDtoWithImage)
                .toList();

        return new PageImpl<>(productDtos, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductResponseDto> getByCategory(Long categoryId, Pageable pageable) {
        try {
            PaintingCategory category = PaintingCategory.values()[categoryId.intValue() - 1];
            logsService.info("Fetching products by category: " + category);

            Page<Product> products = productRepository.findByCategory(category, pageable);
            List<ProductResponseDto> productDtos = products.getContent().stream()
                    .map(this::convertToDtoWithImage)
                    .toList();

            return new PageImpl<>(productDtos, pageable, products.getTotalElements());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid category ID: " + categoryId);
        }
    }

    @Override
    public Page<ProductResponseDto> getByTechnique(Long techniqueId, Pageable pageable) {
        try {
            PaintingTechnique technique = PaintingTechnique.values()[techniqueId.intValue() - 1];
            logsService.info("Fetching products by technique: " + technique);

            Page<Product> products = productRepository.findByTechnique(technique, pageable);
            List<ProductResponseDto> productDtos = products.getContent().stream()
                    .map(this::convertToDtoWithImage)
                    .toList();


            return new PageImpl<>(productDtos, pageable, products.getTotalElements());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid technique ID: " + techniqueId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsBySellerWithoutDeleted(Long sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findProductsBySellerWithoutDeleted(sellerId, pageable);

        return products.map(this::convertToDto);
    }

    @Override
    public Page<ProductResponseDto> getProductsByStatus(Long sellerId, ProductStatus status, Pageable pageable) {
        Page<Product> products = productRepository.findBySellerIdAndStatus(sellerId, status, pageable);

        return products.map(this::convertToDto);
    }

    private ProductResponseDto convertToDtoWithImage(Product product) {
        ProductResponseDto dto = new ProductResponseDto(product);

        String image = imageService.getLatestImage(product.getId());
        if (image != null && !image.isBlank()) {
            dto.setImage(image);
        }

        return dto;
    }

    private ProductResponseDto convertToDto(Product product) {
        String image = imageService.getLatestImage(product.getId());

        return ProductResponseDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .techniques(product.getTechniques() != null ?
                        product.getTechniques().stream().map(Enum::name).toList() :
                        List.of())
                .techniqueEnums(product.getTechniques() != null ?
                        product.getTechniques().stream().toList() :
                        List.of())
                .materials(product.getMaterials())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .image(image != null && !image.isBlank() ? image :
                        product.getImageUrl() != null ? product.getImageUrl() : "")
                .categories(product.getCategories() != null ?
                        product.getCategories().stream().map(Enum::name).toList() :
                        List.of())
                .categoryEnums(product.getCategories() != null ?
                        product.getCategories().stream().toList() :
                        List.of())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getName())
                .build();
    }
}