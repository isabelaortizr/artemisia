package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.address.*;
import com.artemisia_corp.artemisia.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAllAddresses() {
        return ResponseEntity.ok(addressService.listAll());
    }

    @PostMapping
    public ResponseEntity<Void> createAddress(@RequestBody AddressRequestDto addressDto) {
        addressService.save(addressDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateAddress(@RequestBody AddressUpdateDto addressDto) {
        addressService.update(addressDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(new AddressDeleteDto(id));
        return ResponseEntity.ok().build();
    }
}