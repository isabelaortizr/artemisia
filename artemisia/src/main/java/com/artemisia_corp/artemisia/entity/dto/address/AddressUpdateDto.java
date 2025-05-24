package com.artemisia_corp.artemisia.entity.dto.address;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AddressUpdateDto {
    private Long id;
    private Long addressId;
    private String direction;
    private Long user_id;
}
