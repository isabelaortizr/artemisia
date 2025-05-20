package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.Client;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.dto.client.ClientDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientRequestDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientUpdateDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductRequestDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductUpdateDto;
import com.artemisia_corp.artemisia.service.ClientService;
import com.artemisia_corp.artemisia.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping()
    public ResponseEntity<List<Client>> list() {
        try {
            return ResponseEntity.ok(clientService.listAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<List<Void>> save(@RequestBody ClientRequestDto dto) {
        try {
            clientService.save(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Void>> delete(@RequestBody ClientDeleteDto dto) {
        try {
            clientService.delete(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<List<Void>> update(@RequestBody ClientUpdateDto dto) {
        try {
            clientService.update(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
