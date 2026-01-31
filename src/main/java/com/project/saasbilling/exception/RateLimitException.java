package com.project.saasbilling.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitException extends ApiException {

    public RateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}
