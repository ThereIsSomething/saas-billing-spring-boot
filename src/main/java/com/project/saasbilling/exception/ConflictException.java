package com.project.saasbilling.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's a conflict (e.g., duplicate resource).
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}
