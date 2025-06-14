package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.image.ImageUploadDto;
import com.artemisia_corp.artemisia.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadImage(@RequestBody ImageUploadDto dto) {
        imageService.uploadImage(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Secured(value = {"ROLE_ADMIN"})
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
