package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.service.AddressService;
import com.artemisia_corp.artemisia.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDto> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @PostMapping
    public ResponseEntity<AddressResponseDto> createAddress(@RequestBody AddressRequestDto addressDto) {
        AddressResponseDto response = addressService.createAddress(addressDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @PathVariable Long id, @RequestBody AddressRequestDto addressDto) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AddressResponseDto>> getAddressesByUser(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir,

            @RequestParam(name = "nit", required = false) String nit,
            @RequestParam(name = "nombre", required = false) String nombre,

            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = DateUtils.FORMAT_ISO_8601_SHORT) Date from,
            @RequestParam(value = "to" , required = false) @DateTimeFormat(pattern = DateUtils.FORMAT_ISO_8601_SHORT) Date to) {

        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");


        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
            return ResponseEntity.ok(addressService.getAddressesByUser(userId, pageable));
        } catch (OperationException e) {
            log.error("Error al listar el empresas. Causa:{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error al listar el empresas", e);
            return ResponseEntity.badRequest().build();
        }
    }
}