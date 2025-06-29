package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateOrderDetailDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    void assignAddressToNotaVenta(SetAddressDto setAddressDto);
    EstdoNotaVentaResponseDto obtenerEstadoTransaccion(Long userId);
    NotaVentaResponseDto getActiveCartByUserId(Long userId);
    NotaVentaResponseDto addProductToCart(AddToCartDto addToCartDto);
    StereumPagaResponseDto getPaymentInfo(RequestPaymentDto request);
    NotaVentaResponseDto updateOrderDetailStock(UpdateOrderDetailDto updateOrderDetailDto);
}
