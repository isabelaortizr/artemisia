package com.artemisia_corp.artemisia.exception;

public class InvalidJwtAuthenticationException extends RuntimeException {
    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
}
