package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaResponseDto;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@RequestMapping("/api/notas-venta")
@Tag(name = "Sales Note Management", description = "Endpoints for managing sales notes")
public class NotaVentaController {

    @Autowired
    private NotaVentaService notaVentaService;

    @Operation(summary = "Get all sales notes", description = "Returns paginated list of all sales notes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales notes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<NotaVentaResponseDto>> getAllNotasVenta(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(notaVentaService.getAllNotasVenta(pageable));
    }

    @Operation(summary = "Get sales note by ID", description = "Returns a single sales note by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales note found",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Sales note not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotaVentaResponseDto> getNotaVentaById(@PathVariable Long id) {
        return ResponseEntity.ok(notaVentaService.getNotaVentaById(id));
    }

    @Operation(summary = "Create a new sales note", description = "Creates a new sales note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sales note created successfully",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<NotaVentaResponseDto> createNotaVenta(@RequestBody NotaVentaRequestDto notaVentaDto) {
        NotaVentaResponseDto response = notaVentaService.createNotaVenta(notaVentaDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a sales note", description = "Updates an existing sales note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales note updated successfully",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Sales note not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<NotaVentaResponseDto> updateNotaVenta(
            @PathVariable Long id,
            @RequestBody NotaVentaRequestDto notaVentaDto) {
        return ResponseEntity.ok(notaVentaService.updateNotaVenta(id, notaVentaDto));
    }

    @Operation(summary = "Delete a sales note", description = "Deletes a sales note by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sales note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Sales note not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotaVenta(@PathVariable Long id) {
        notaVentaService.deleteNotaVenta(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Complete a sales note", description = "Marks a sales note as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales note completed successfully"),
            @ApiResponse(responseCode = "404", description = "Sales note not found")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeNotaVenta(@PathVariable Long id) {
        notaVentaService.completeNotaVenta(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel a sales note", description = "Marks a sales note as cancelled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales note cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Sales note not found")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelNotaVenta(@PathVariable Long id) {
        notaVentaService.cancelarNotaVenta(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get sales notes by status", description = "Returns paginated list of sales notes filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales notes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
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

    @Operation(summary = "Get user sales history", description = "Returns paginated list of completed sales for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
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

    @Operation(summary = "Add product to cart", description = "Adds a product to the user's shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Product or user not found")
    })
    @PostMapping("/add")
    public ResponseEntity<NotaVentaResponseDto> addToCart(@RequestBody AddToCartDto addToCartDto) {
        return new ResponseEntity<>(notaVentaService.addProductToCart(addToCartDto), HttpStatus.OK);
    }

    @Operation(summary = "Get user's active cart", description = "Returns the active shopping cart for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found or no active cart")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotaVentaResponseDto> getCart(@PathVariable Long userId) {
        return new ResponseEntity<>(notaVentaService.getActiveCartByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create payment transaction", description = "Creates a payment transaction for a sales note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = StereumPagaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create_transaction")
    public ResponseEntity<StereumPagaResponseDto> conseguirTransaccion(@RequestBody RequestPaymentDto respuesta) {
        try {
            return ResponseEntity.ok(notaVentaService.getPaymentInfo(respuesta));
        } catch (Exception e) {
            log.error("Error al verificar la transacción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Verify payment transaction", description = "Verifies the status of a payment transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/verify_transaction")
    public ResponseEntity<Void> verificarTransaccion(@RequestBody RespuestaVerificacionNotaVentaDto respuesta) {
        try {
            notaVentaService.obtenerRespuestaTransaccion(respuesta);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error al verificar la transacción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}