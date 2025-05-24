package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas_venta")
@RequiredArgsConstructor
public class NotaVentaController {
    @Autowired
    private NotaVentaService notaVentaService;

    @GetMapping("/with_customer")
    public ResponseEntity<List<NotaVentaResponseWCustomerDto>> getAllNotasVentaWithCustomer() {
        return ResponseEntity.ok(notaVentaService.listWithBuyer());
    }

    @GetMapping
    public ResponseEntity<List<NotaVentaResponseDto>> getAllNotasVenta() {
        return ResponseEntity.ok(notaVentaService.listAll());
    }

    @PostMapping
    public ResponseEntity<Void> createNotaVenta(@RequestBody NotaVentaRequestDto notaVentaDto) {
        notaVentaService.save(notaVentaDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateNotaVenta(@RequestBody NotaVentaUpdateDto notaVentaDto) {
        notaVentaService.update(notaVentaDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteNotaVenta(@RequestBody Long id) {
        notaVentaService.delete(new NotaVentaDeleteDto(id));
        return ResponseEntity.ok().build();
    }
}