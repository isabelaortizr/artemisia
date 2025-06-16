package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;

public interface ImageService {
    void uploadImage(ImageUploadDto dto);
    void deleteImage(Long id);
}
