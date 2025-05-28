package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaRequestDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/notas-venta")
public class NotaVentaController {

    @Autowired
    private NotaVentaService notaVentaService;

    @GetMapping
    public ResponseEntity<List<NotaVentaResponseDto>> getAllNotasVenta() {
        return ResponseEntity.ok(notaVentaService.getAllNotasVenta());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaVentaResponseDto> getNotaVentaById(@PathVariable Long id) {
        return ResponseEntity.ok(notaVentaService.getNotaVentaById(id));
    }

    @PostMapping
    public ResponseEntity<NotaVentaResponseDto> createNotaVenta(@RequestBody NotaVentaRequestDto notaVentaDto) {
        NotaVentaResponseDto response = notaVentaService.createNotaVenta(notaVentaDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotaVentaResponseDto> updateNotaVenta(
            @PathVariable Long id,
            @RequestBody NotaVentaRequestDto notaVentaDto) {
        return ResponseEntity.ok(notaVentaService.updateNotaVenta(id, notaVentaDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotaVenta(@PathVariable Long id) {
        notaVentaService.deleteNotaVenta(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeNotaVenta(@PathVariable Long id) {
        notaVentaService.completeNotaVenta(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<NotaVentaResponseDto>> getNotasVentaByEstado(
            @PathVariable VentaEstado estado) {
        return ResponseEntity.ok(notaVentaService.getNotasVentaByEstado(estado));
    }
}