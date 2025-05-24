package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;

import java.util.List;

public interface NotaVentaService {
    List<NotaVentaResponseWCustomerDto> listWithBuyer();
    List<NotaVentaResponseDto> listAll();
    void save(NotaVentaRequestDto notaVentaRequestDto);
    void delete(NotaVentaDeleteDto notaVentaDeleteDto);
    void update(NotaVentaUpdateDto notaVentaUpdateDto);

}
