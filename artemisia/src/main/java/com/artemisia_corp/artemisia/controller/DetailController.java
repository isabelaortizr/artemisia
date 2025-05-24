package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.datail.*;
import com.artemisia_corp.artemisia.service.DetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/details")
@RequiredArgsConstructor
public class DetailController {
    @Autowired
    private DetailService detailService;

    @GetMapping
    public ResponseEntity<List<DetailResponseDto>> getAllDetails() {
        return ResponseEntity.ok(detailService.listAll());
    }

    @GetMapping("/simple")
    public ResponseEntity<List<SimpleDetailDto>> getAllSimpleDetails() {
        return ResponseEntity.ok(detailService.getAllSimpleDetails());
    }

    @GetMapping("/by_nota_venta")
    public ResponseEntity<List<SimpleDetailDto>> getDetailsByNotaVenta(@RequestBody Long notaVentaId) {
        return ResponseEntity.ok(detailService.getDetailsByNotaVenta(notaVentaId));
    }

    @PostMapping
    public ResponseEntity<Void> createDetail(@RequestBody DetailRequestDto detailDto) {
        detailService.save(detailDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateDetail(@RequestBody DetailUpdateDto detailDto) {
        detailService.update(detailDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteDetail(@RequestBody Long id) {
        detailService.delete(new DetailDeleteDto(id));
        return ResponseEntity.ok().build();
    }
}