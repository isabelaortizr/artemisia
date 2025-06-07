package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaRequestDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;

import java.util.List;

public interface NotaVentaService {
    List<NotaVentaResponseDto> getAllNotasVenta();
    NotaVentaResponseDto getNotaVentaById(Long id);
    NotaVentaResponseDto createNotaVenta(NotaVentaRequestDto notaVentaDto);
    NotaVentaResponseDto updateNotaVenta(Long id, NotaVentaRequestDto notaVentaDto);
    void deleteNotaVenta(Long id);
    void completeNotaVenta(Long id);
    void cancelarNotaVenta(Long id);
    List<NotaVentaResponseDto> getNotasVentaByEstado(VentaEstado estado);
}