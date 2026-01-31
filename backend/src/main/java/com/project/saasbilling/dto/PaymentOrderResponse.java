package com.project.saasbilling.dto;

import com.project.saasbilling.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment order response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private String id;
    private String orderId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String planId;
    private String planName;
    private boolean verified;
    private String message;
    private LocalDateTime createdAt;
}
