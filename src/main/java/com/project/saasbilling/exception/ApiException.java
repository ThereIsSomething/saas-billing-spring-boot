package com.project.saasbilling.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for API errors with HTTP status code.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = status.name();
    }

    public ApiException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
