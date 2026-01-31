package com.project.saasbilling.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentException extends ApiException {

    public PaymentException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }

    public PaymentException(String message, String code) {
        super(message, HttpStatus.PAYMENT_REQUIRED, code);
    }
}
