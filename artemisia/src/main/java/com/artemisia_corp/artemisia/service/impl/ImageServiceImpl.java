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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final LogsService logsService;

    @Override
    public void uploadImage(ImageUploadDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotDataFoundException("Product not found"));

        Image image = Image.builder()
                .fileName(dto.getFileName())
                .base64Data(dto.getBase64Image())
                .product(product)
                .build();

        imageRepository.save(image);
        logsService.info("Image uploaded to DB for product ID: " + product.getId());
    }

    @Override
    public void deleteImage(Long id) {
        if (!imageRepository.existsById(id)) {
            logsService.error("Image not found with ID: " + id);
            throw new NotDataFoundException("Image not found");
        }
        imageRepository.deleteById(id);
        logsService.info("Image deleted with ID: " + id);
    }

    @Override
    public String getLatestImage(Long productId) {
        logsService.info("Fetching latest image for product ID: " + productId);
        if (!productRepository.existsById(productId)) {
            logsService.error("Product not found with ID: " + productId);
            throw new NotDataFoundException("Product not found");
        }
        String image = imageRepository.findLastBase64DataByProductId(productId);
        if (image == null || image.isEmpty()) {
            logsService.error("No image found for product ID: " + productId);
            return null;
        }
        return image;
    }
}

