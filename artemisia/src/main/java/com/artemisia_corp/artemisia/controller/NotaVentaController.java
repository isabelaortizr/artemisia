package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.AddToCartDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaRequestDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Page<NotaVentaResponseDto>> getAllNotasVenta(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(notaVentaService.getAllNotasVenta(pageable));
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

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelNotaVenta(@PathVariable Long id) {
        notaVentaService.cancelarNotaVenta(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<NotaVentaResponseDto>> getNotasVentaByEstado(
            @PathVariable VentaEstado estado,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(notaVentaService.getNotasVentaByEstado(estado, pageable));
    }

    @GetMapping("/historial-usuario/{id}")
    public ResponseEntity<Page<NotaVentaResponseDto>> getHistory(
            @RequestBody @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(notaVentaService.getCompletedSalesByUser(id, pageable));
    }

    @PostMapping("/add")
    public ResponseEntity<NotaVentaResponseDto> addToCart(@RequestBody AddToCartDto addToCartDto) {
        return new ResponseEntity<>(notaVentaService.addProductToCart(addToCartDto), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<NotaVentaResponseDto> getCart(@PathVariable Long userId) {
        return new ResponseEntity<>(notaVentaService.getActiveCartByUserId(userId), HttpStatus.OK);
    }


}