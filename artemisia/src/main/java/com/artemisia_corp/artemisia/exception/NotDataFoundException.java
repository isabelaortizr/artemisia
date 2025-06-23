package com.artemisia_corp.artemisia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Error de sistema")
@Getter
public class NotDataFoundException extends RuntimeException {

    public NotDataFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotDataFoundException(String message) {
        super(message);
    }

}
