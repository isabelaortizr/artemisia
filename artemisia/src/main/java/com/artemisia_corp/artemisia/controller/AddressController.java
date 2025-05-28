package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
    @RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

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
    public ResponseEntity<List<AddressResponseDto>> getAddressesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getAddressesByUser(userId));
    }
}