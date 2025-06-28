package com.artemisia_corp.artemisia.integracion;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.*;

public interface SterumPayService {
    StereumAuthResponse obtenerTokenAutenticacion();
    StereumPagaResponseDto crearCargoCobro(StereumPagaDto chargeDTO, Long idNotaVenta);
    EstadoResponseDto obtenerEstadoCobro(String id_transaccion);
    NotaVentaResponseDto conversionBob (ConversionDto conversionDto);
    EstadoResponseDto cancelarCargo(String id_transaccion);
}
