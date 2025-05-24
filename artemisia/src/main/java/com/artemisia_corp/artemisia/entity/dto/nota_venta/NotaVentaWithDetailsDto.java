package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.artemisia_corp.artemisia.entity.dto.datail.DetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaVentaWithDetailsDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long buyerAddressId;
    private String buyerAddress;
    private String estadoVenta;
    private Double totalGlobal;
    private LocalDateTime date;
    private List<DetailResponseDto> details;
}