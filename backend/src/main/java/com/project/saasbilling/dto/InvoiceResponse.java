package com.project.saasbilling.dto;

import com.project.saasbilling.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for invoice response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String id;
    private String invoiceNumber;
    private String userId;
    private String userEmail;
    private String userName;
    private String subscriptionId;
    private String planName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private InvoiceStatus status;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private String notes;
    private String pdfUrl;
    private LocalDateTime createdAt;
}
