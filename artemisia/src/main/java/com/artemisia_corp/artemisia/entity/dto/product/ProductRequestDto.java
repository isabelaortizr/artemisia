package com.artemisia_corp.artemisia.entity.dto.product;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ProductRequestDto {
    String name;
    String description;
    BigDecimal price;
    int stock;
    long companyId;
}
