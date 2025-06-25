package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.service.AddressService;
import com.artemisia_corp.artemisia.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Address Management", description = "Endpoints for managing addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Operation(summary = "Get address by ID", description = "Returns a single address by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address found",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDto> getAddressById(@PathVariable Long id) {
        AddressResponseDto response = addressService.getAddressById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new address", description = "Creates a new address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address created successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<AddressResponseDto> createAddress(@RequestBody AddressRequestDto addressDto) {
        AddressResponseDto response = addressService.createAddress(addressDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update an address", description = "Updates an existing address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address updated successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @PathVariable Long id, @RequestBody AddressRequestDto addressDto) {
        AddressResponseDto response = addressService.updateAddress(id, addressDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an address", description = "Deletes an address by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get addresses by user", description = "Returns paginated list of addresses for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
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
            Page<AddressResponseDto> response = addressService.getAddressesByUser(userId, pageable);
            return ResponseEntity.ok(response);
        } catch (OperationException e) {
            log.error("Error al listar el empresas. Causa:{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error al listar el empresas", e);
            return ResponseEntity.badRequest().build();
        }
    }
}