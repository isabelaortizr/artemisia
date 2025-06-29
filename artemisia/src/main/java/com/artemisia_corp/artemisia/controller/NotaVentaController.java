package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateOrderDetailDto;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaResponseDto;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@Controller
    @RequestMapping("/api/notas-venta")
@Tag(name = "Sales Note Management", description = "Endpoints for managing sales notes")
public class NotaVentaController {

    @Autowired
    private NotaVentaService notaVentaService;
    @Value("${stereum-pay.api-key}")
    private String apiKey;

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

    @Operation(summary = "Assign an address to a sale", description = "Assigns an address to an existing sales note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address assigned to sales note successfully"),
            @ApiResponse(responseCode = "404", description = "Sales note or address not found")
    })
    @PutMapping("/set_address")
    public ResponseEntity<Void> assignAddressToNotaVenta(
            @RequestBody SetAddressDto setAddressDto) {

        notaVentaService.assignAddressToNotaVenta(setAddressDto);
        return ResponseEntity.ok().build();
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
        return ResponseEntity.ok(notaVentaService.getPaymentInfo(respuesta));
    }

    @Operation(summary = "Verify payment transaction", description = "Verifies the status of a payment transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/verify_transaction/{userId}")
    public ResponseEntity<Void> verificarTransaccion(@PathVariable Long userId) {
        notaVentaService.obtenerEstadoTransaccion(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update stock for OrderDetail", description = "Updates the stock quantity for a specific product in a user's active cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order detail stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    @PutMapping("/order_detail/update_stock")
    public ResponseEntity<NotaVentaResponseDto> updateOrderDetailStock(@RequestBody UpdateOrderDetailDto updateOrderDetailDto) {
        NotaVentaResponseDto nv = notaVentaService.updateOrderDetailStock(updateOrderDetailDto);
        return ResponseEntity.ok(nv);
    }

    @PostMapping(value = "/inbound", produces = MediaType.APPLICATION_JSON_VALUE, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<String> outbound(
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-timestamp") long xTimestamp,
            @RequestBody String body) {

        log.info("Received inbound request with headers - x-signature: {}, x-timestamp: {}", signature, xTimestamp);
        log.info("Request body received: {}", body);

        var currentTime = System.currentTimeMillis() / 1000;
        if (Math.abs(currentTime - xTimestamp) < 300) {
            log.warn("Timestamp validation failed. Current time: {}, received timestamp: {}, difference: {} seconds",
                    currentTime, xTimestamp, Math.abs(currentTime - xTimestamp));
            throw new OperationException("Firma inválida");
        }

        String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256,
                apiKey.getBytes(StandardCharsets.UTF_8))
                .hmacHex(body.getBytes(StandardCharsets.UTF_8));

        log.info("Calculated HMAC: {}", hmac);

        if (!signature.equals(hmac)) {
            log.error("Signature validation failed. Expected: {}, Actual: {}", hmac, signature);
            throw new OperationException("Error en la firma");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RespuestaVerificacionNotaVentaDto transaction;
        try {
            transaction = objectMapper.readValue(body, RespuestaVerificacionNotaVentaDto.class);
            log.info("Successfully parsed request body to RespuestaVerificacionNotaVentaDto: {}", transaction);
        } catch (Exception e) {
            log.error("Failed to parse request body. Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error en la firma");
        }

        if (transaction.getNotificationType().equals("test")) {
            log.info("Received test notification");
            return ok().build();
        }

        if (!transaction.getNotificationType().equals("transaction")) {
            log.warn("Invalid notification type received: {}", transaction.getNotificationType());
            throw new OperationException("No corresponde a este método la notificación");
        }

        try {
            if (transaction.getId() == null) {
                log.error("Transaction ID is null in the received notification");
                return ResponseEntity.badRequest().body("No se encontró el id de la transacción");
            }

            log.info("Processing transaction notification for ID: {}", transaction.getId());
            notaVentaService.obtenerRespuestaTransaccion(transaction);

            return ok().build();
        } catch (OperationException e) {
            log.error("OperationException while processing Circle notification for transaction {}. Cause: {}",
                    transaction != null ? transaction.getId() : "null", e.getMessage(), e);
            throw new OperationException("Se generó un error al recibir notificación de circle");
        } catch (Exception e) {
            log.error("Unexpected error while processing Circle confirmation for transaction {}",
                    transaction != null ? transaction.getId() : "null", e);
            throw new OperationException("Se generó un error al recibir la confirmación de circle");
        }
    }
}