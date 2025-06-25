package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class SetAddressDto {
    private Long userId;
    private Long addressId;
}
