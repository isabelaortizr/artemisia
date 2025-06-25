package com.artemisia_corp.artemisia.entity.dto.security;

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
    @JsonProperty("username")
    private String username;
    private String id;
}
