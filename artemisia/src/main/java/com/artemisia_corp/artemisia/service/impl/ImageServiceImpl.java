package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Image;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.repository.ImageRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final LogsService logsService;

    @Override
    public void uploadImage(ImageUploadDto dto) {
        User seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Image image = Image.builder()
                .fileName(dto.getFileName())
                .base64Data(dto.getBase64Image())
                .seller(seller)
                .build();

        imageRepository.save(image);
        logsService.info("Image uploaded to DB for seller ID: " + seller.getId());
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

