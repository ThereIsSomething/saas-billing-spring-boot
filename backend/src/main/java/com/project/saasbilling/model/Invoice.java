package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice document representing a billing invoice.
 */
@Document(collection = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private String id;

    @Indexed(unique = true)
    private String invoiceNumber;

    @Indexed
    private String userId;

    @Indexed
    private String subscriptionId;

    // Denormalized for easy access
    private String userEmail;
    private String userName;
    private String planName;

    private BigDecimal amount;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal totalAmount;

    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private LocalDate paidDate;

    private LocalDate billingPeriodStart;

    private LocalDate billingPeriodEnd;

    private String notes;

    private String pdfUrl;

    @CreatedDate
    private LocalDateTime createdAt;
}
