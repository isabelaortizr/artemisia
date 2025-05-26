package com.artemisia_corp.artemisia.entity.dto.address;

import com.artemisia_corp.artemisia.entity.Address;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AddressResponseDto {
    private Long addressId;
    private String direction;
    private Long userId;

    public AddressResponseDto(Address address) {
        this.addressId = address.getAddressId();
        this.direction = address.getDirection();
        this.userId = address.getUser().getId();
    }
}