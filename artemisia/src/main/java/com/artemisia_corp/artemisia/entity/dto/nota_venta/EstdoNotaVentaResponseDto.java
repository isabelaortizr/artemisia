package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstdoNotaVentaResponseDto {
    private String estado;
    private Long notaVentaId;
}
