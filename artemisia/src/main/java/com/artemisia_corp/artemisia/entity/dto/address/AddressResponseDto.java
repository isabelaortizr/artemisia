package com.artemisia_corp.artemisia.entity.dto.address;

import com.artemisia_corp.artemisia.entity.Address;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AddressResponseDto {
    @JsonProperty("address_id")
    private Long addressId;
    @JsonProperty("recipient_name")
    private String recipientName;
    @JsonProperty("recipient_surname")
    private String recipientSurname;
    private String country;
    private String city;
    private String street;
    @JsonProperty("house_number")
    private String houseNumber;
    private String extra;
    private Long userId;

    public AddressResponseDto(Address address) {
        this.addressId = address.getId();
        this.recipientName = address.getRecipientName();
        this.recipientSurname = address.getRecipientSurname();
        this.country = address.getCountry();
        this.city = address.getCity();
        this.street = address.getStreet();
        this.houseNumber = address.getHouseNumber();
        this.extra = address.getExtra();
        this.userId = address.getUser().getId();
    }
}