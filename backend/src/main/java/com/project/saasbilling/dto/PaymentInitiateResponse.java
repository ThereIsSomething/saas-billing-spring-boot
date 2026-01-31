package com.project.saasbilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment initiation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String planName;
    private String keyId; // Mock Razorpay key ID
    private boolean requiresPayment;
    private String message;
}
