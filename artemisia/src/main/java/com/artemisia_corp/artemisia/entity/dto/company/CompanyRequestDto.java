package com.artemisia_corp.artemisia.entity.dto.company;

import com.artemisia_corp.artemisia.entity.enums.CompanyStateEntity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CompanyRequestDto {
    private String name;
    private String nit;
    private CompanyStateEntity state;
}
