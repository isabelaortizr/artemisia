package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.integracion.SterumPayService;
import com.artemisia_corp.artemisia.integracion.impl.dtos.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/stereum-pay")
public class SterumPayController {
    private SterumPayService sterumPayService;

    @GetMapping("/token")
    public ResponseEntity<StereumAuthResponse> obtenerTokenAutenticacion() {
        log.info("Login sterum");
        try {
            StereumAuthResponse response = sterumPayService.obtenerTokenAutenticacion();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/crear-cargo/{id_nota_venta}")
    public ResponseEntity<StereumPagaResponseDto> crearCargoCobro(@RequestBody StereumPagaDto chargeDto,
                                                                  @PathVariable("id_nota_venta") Long idNotaVenta) {
        try {
            StereumPagaResponseDto response = sterumPayService.crearCargoCobro(chargeDto, idNotaVenta);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al crear el cargo de cobro.", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/conversion_moneda")
    public ResponseEntity<NotaVentaResponseDto> conversionBob(
            @RequestBody ConversionDto conversionDto) {
        try {
            NotaVentaResponseDto response = sterumPayService.conversionBob(conversionDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al realizar la conversion de bob.", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/estado-cobro/{idTransaccion}")
    public ResponseEntity<EstadoResponseDto> obtenerEstadoCobro(@PathVariable("idTransaccion") String idTransaccion) {
        try {
            return ResponseEntity.ok(sterumPayService.obtenerEstadoCobro(idTransaccion));
        } catch (Exception e) {
            log.error("Error al obtener el estado del cobro.", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/cancelar-cargo/{idTransaccion}")
    public ResponseEntity<EstadoResponseDto> cancelarCargo(@PathVariable("idTransaccion") String idTransaccion) {
        try {
            return ResponseEntity.ok(sterumPayService.cancelarCargo(idTransaccion));
        } catch (Exception e) {
            log.error("Error al cancelar el cargo.", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}