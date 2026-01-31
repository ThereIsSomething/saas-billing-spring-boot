package com.project.saasbilling.dto;

import com.project.saasbilling.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String invoiceId;
    private String invoiceNumber;
    private String transactionId;
    private String externalPaymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String paymentGateway;
    private String failureReason;
    private BigDecimal refundedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
