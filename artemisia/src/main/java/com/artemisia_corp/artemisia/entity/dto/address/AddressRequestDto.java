package com.artemisia_corp.artemisia.entity.dto.address;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AddressRequestDto {
    private String direction;
    private long user_id;
}
