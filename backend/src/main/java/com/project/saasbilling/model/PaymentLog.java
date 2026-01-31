package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment log document representing a payment transaction.
 */
@Document(collection = "payment_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String invoiceId;

    // Denormalized for easy access
    private String userEmail;
    private String invoiceNumber;

    @Indexed(unique = true)
    private String transactionId;

    private String externalPaymentId;

    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethod;

    private String paymentGateway;

    private String failureReason;

    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
