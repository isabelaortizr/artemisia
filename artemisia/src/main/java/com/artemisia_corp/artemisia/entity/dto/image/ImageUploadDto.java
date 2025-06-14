package com.artemisia_corp.artemisia.entity.dto.image;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadDto {
    private Long productId;
    private String fileName;
    private String base64Image;
}
