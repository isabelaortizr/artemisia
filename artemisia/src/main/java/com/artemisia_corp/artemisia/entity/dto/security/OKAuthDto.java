package com.upb.modulo_01.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OKAuthDto {
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("refresh_token")
    private String username;

}
