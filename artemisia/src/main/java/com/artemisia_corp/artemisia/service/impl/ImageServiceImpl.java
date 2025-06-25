package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Image;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.ImageRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final LogsService logsService;

    @Override
    public void uploadImage(ImageUploadDto dto) {
        if (dto.getProductId() == null) {
            log.error("Product ID is required.");
            logsService.error("Product ID is required.");
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (dto.getFileName() == null || dto.getFileName().trim().isEmpty()) {
            log.error("File name is required.");
            logsService.error("File name is required.");
            throw new IllegalArgumentException("File name is required.");
        }
        if (dto.getBase64Image() == null || dto.getBase64Image().trim().isEmpty()) {
            log.error("Base64 image data is required.");
            logsService.error("Base64 image data is required.");
            throw new IllegalArgumentException("Base64 image data is required.");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with ID: " + dto.getProductId());
                    logsService.error("Product not found with ID: " + dto.getProductId());
                    return new NotDataFoundException("Product not found with ID: " + dto.getProductId());
                });

        Image image = Image.builder()
                .fileName(dto.getFileName())
                .base64Data(dto.getBase64Image())
                .product(product)
                .build();

        Image savedImage = imageRepository.save(image);
        image.setFileName(image.getFileName() + savedImage.getId());
        logsService.info("Image uploaded to DB for product ID: " + product.getId());
    }

    @Override
    public void deleteImage(Long id) {
        if (id == null) {
            log.error("Image ID is required.");
            logsService.error("Image ID is required.");
            throw new IllegalArgumentException("Image ID is required.");
        }
        if (!imageRepository.existsById(id)) {
            log.error("Image not found with ID: " + id);
            logsService.error("Image not found with ID: " + id);
            throw new NotDataFoundException("Image not found with ID: " + id);
        }
        imageRepository.deleteById(id);
        logsService.info("Image deleted with ID: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLatestImage(Long productId) {
        if (productId == null) {
            log.error("Product ID is required.");
            logsService.error("Product ID is required.");
            throw new IllegalArgumentException("Product ID is required.");
        }

        logsService.info("Fetching latest image for product ID: " + productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: " + productId);
                    logsService.error("Product not found with ID: " + productId);
                    return new NotDataFoundException("Product not found with ID: " + productId);
                });

        String image = imageRepository.findLastBase64DataByProductId(product.getId());
        return image;
    }
}

