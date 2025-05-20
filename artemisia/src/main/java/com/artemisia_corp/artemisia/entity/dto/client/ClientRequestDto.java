package com.artemisia_corp.artemisia.entity.dto.client;

import com.artemisia_corp.artemisia.entity.enums.ClientStateEntiy;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ClientRequestDto {
    String name;
    int nit;
    ClientStateEntiy state;
}
