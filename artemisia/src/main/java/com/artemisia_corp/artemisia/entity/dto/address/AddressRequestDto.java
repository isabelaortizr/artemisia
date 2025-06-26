package com.artemisia_corp.artemisia.entity.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class AddressRequestDto {
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
    @JsonProperty("user_id")
    private Long userId;
}