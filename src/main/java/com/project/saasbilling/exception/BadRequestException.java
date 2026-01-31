package com.project.saasbilling.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's a bad request or validation error.
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, String code) {
        super(message, HttpStatus.BAD_REQUEST, code);
    }
}
