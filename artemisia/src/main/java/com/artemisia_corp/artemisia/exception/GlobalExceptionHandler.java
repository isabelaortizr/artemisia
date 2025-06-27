package com.artemisia_corp.artemisia.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClientNotFoundException(ClientNotFoundException ex) {
        log.error("ClientNotFoundException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Client error", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncompleteAddressException.class)
    public ResponseEntity<Map<String, Object>> handleIncompleteAddressException(IncompleteAddressException ex) {
        log.error("IncompleteAddressException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Address error", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotDataFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotDataFoundException(NotDataFoundException ex) {
        log.error("NotDataFoundException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Data Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperationException.class)
    public ResponseEntity<Map<String, Object>> handleOperationException(OperationException ex) {
        log.error("OperationException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Data Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Data Not Found", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Invalid Input", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeExeption occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Invalid Input", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse("Internal Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("MethodArgumentNotValidException occurred. Validation errors: {}", fieldErrors, ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String error, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }

}