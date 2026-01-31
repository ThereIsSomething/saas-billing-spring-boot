package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MongoDB document for payment orders (mock Razorpay implementation).
 */
@Document(collection = "payment_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String planId;

    private BigDecimal amount;

    private String currency;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // Mock Razorpay-like fields
    @Indexed(unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    // Denormalized fields for display
    private String userEmail;
    private String planName;

    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
