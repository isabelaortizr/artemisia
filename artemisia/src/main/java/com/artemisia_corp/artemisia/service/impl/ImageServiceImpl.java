package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Image;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.ImageRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final LogsService logsService;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public void uploadImage(ImageUploadDto dto) {
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (dto.getFileName() == null || dto.getFileName().trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required.");
        }
        if (dto.getBase64Image() == null || dto.getBase64Image().trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 image data is required.");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotDataFoundException("Product not found with ID: " + dto.getProductId()));

        try {
            byte[] imageBytes = Base64.getDecoder().decode(dto.getBase64Image());

            Map uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                    "folder", "artemisia/products",
                    "public_id", "product_" + dto.getProductId() + "_" + System.currentTimeMillis(),
                    "resource_type", "auto"
            ));

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Image image = Image.builder()
                    .fileName(dto.getFileName())
                    .cloudinaryUrl(cloudinaryUrl)
                    .publicId(publicId)
                    .product(product)
                    .build();

            imageRepository.save(image);
            logsService.info("Image uploaded to Cloudinary for product ID: " + product.getId());

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary for product {}: {}", dto.getProductId(), e.getMessage());
            logsService.error("Cloudinary upload failed for product ID: " + dto.getProductId());
            throw new RuntimeException("Error uploading image to Cloudinary.", e);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Image ID is required.");
        }

        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("Image not found with ID: " + id));

        try {
            cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Could not delete image from Cloudinary (publicId: {}): {}", image.getPublicId(), e.getMessage());
        }

        imageRepository.deleteById(id);
        logsService.info("Image deleted with ID: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLatestImage(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }

        return imageRepository.findLatestCloudinaryUrlByProductId(productId);
    }
}
