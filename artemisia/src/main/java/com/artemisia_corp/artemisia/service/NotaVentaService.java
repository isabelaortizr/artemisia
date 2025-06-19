package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.AddToCartDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaRequestDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.RespuestaVerificacionNotaVentaDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotaVentaService {
    Page<NotaVentaResponseDto> getAllNotasVenta(Pageable pageable);
    NotaVentaResponseDto getNotaVentaById(Long id);
    NotaVentaResponseDto createNotaVenta(NotaVentaRequestDto notaVentaDto);
    NotaVentaResponseDto updateNotaVenta(Long id, NotaVentaRequestDto notaVentaDto);
    void deleteNotaVenta(Long id);
    void completeNotaVenta(Long id);
    void cancelarNotaVenta(Long id);
    Page<NotaVentaResponseDto> getNotasVentaByEstado(VentaEstado estado, Pageable pageable);
    Page<NotaVentaResponseDto> getCompletedSalesByUser(Long userId, Pageable pageable);
    void ingresarIdTransaccion(String idTransaccion, Long notaVentaId);
    void obtenerRespuestaTransaccion(RespuestaVerificacionNotaVentaDto respuesta);
    NotaVentaResponseDto getActiveCartByUserId(Long userId);
    NotaVentaResponseDto addProductToCart(AddToCartDto addToCartDto);
}
