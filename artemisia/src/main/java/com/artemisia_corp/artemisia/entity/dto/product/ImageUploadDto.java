package com.artemisia_corp.artemisia.entity.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadDto {
    private Long sellerId;
    private String fileName;
    private String base64Image;
}
