package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Image;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
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
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Image image = Image.builder()
                .fileName(dto.getFileName())
                .base64Data(dto.getBase64Image())
                .product_id(product)
                .build();

        imageRepository.save(image);
        logsService.info("Image uploaded to DB for product ID: " + product.getProductId());
    }

    @Override
    public void deleteImage(Long id) {
        if (!imageRepository.existsById(id)) {
            logsService.error("Image not found with ID: " + id);
            throw new RuntimeException("Image not found");
        }
        imageRepository.deleteById(id);
        logsService.info("Image deleted with ID: " + id);
    }
}

