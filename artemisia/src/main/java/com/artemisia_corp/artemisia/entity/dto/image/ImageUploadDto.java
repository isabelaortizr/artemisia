package com.artemisia_corp.artemisia.entity.dto.image;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class ImageUploadDto {
    private Long productId;
    private String fileName;
    private String base64Image;

    public ImageUploadDto(Long productId, String fileName, String base64Image) {
        this.productId = productId;
        this.fileName = fileName;
        this.base64Image = base64Image;
    }
}
