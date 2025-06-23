package com.artemisia_corp.artemisia.integracion;

import com.artemisia_corp.artemisia.integracion.impl.dtos.*;

public interface SterumPayService {
    StereumAuthResponse obtenerTokenAutenticacion();
    StereumPagaResponseDto crearCargoCobro(StereumPagaDto chargeDTO, Long idNotaVenta);
    EstadoResponseDto obtenerEstadoCobro(String id_transaccion);
    CurrencyConversionResponseDto conversionBob (CurrencyConversionDto conversionEntity);
    EstadoResponseDto cancelarCargo(String id_transaccion);
}
