package com.project.saasbilling.service;

import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.dto.PaymentRequest;
import com.project.saasbilling.dto.PaymentResponse;
import com.project.saasbilling.exception.BadRequestException;
import com.project.saasbilling.exception.PaymentException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.InvoiceRepository;
import com.project.saasbilling.repository.PaymentLogRepository;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for payment operations.
 * Updated for MongoDB with denormalized data model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentLogRepository paymentLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;
    private final EmailService emailService;
    private final DtoMapper dtoMapper;

    /**
     * Process a payment for an invoice.
     */
    public PaymentResponse processPayment(String userId, PaymentRequest request, String ipAddress) {
        log.info("Processing payment for invoice: {} by user: {}", request.getInvoiceId(), userId);

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("Invoice does not belong to this user");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay a cancelled invoice");
        }

        // Validate payment amount
        if (request.getAmount().compareTo(invoice.getTotalAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match invoice total");
        }

        // Create payment log with denormalized data
        PaymentLog payment = PaymentLog.builder()
                .userId(userId)
                .invoiceId(invoice.getId())
                // Denormalized fields
                .userEmail(invoice.getUserEmail())
                .invoiceNumber(invoice.getInvoiceNumber())
                .transactionId(generateTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : invoice.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "card")
                .paymentGateway("razorpay")
                .build();

        payment = paymentLogRepository.save(payment);

        try {
            // Process payment through Razorpay (mock)
            RazorpayService.PaymentResult result = razorpayService.processPayment(
                    payment.getTransactionId(),
                    request.getAmount(),
                    payment.getCurrency());

            if (result.isSuccess()) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setExternalPaymentId(result.getPaymentId());
                payment.setProcessedAt(LocalDateTime.now());

                // Update invoice status
                invoice.setStatus(InvoiceStatus.PAID);
                invoice.setPaidDate(LocalDate.now());
                invoiceRepository.save(invoice);

                log.info("Payment successful: {}", payment.getTransactionId());

                // Send payment confirmation email
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    emailService.sendPaymentConfirmationEmail(user, payment, invoice);
                }
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(result.getErrorMessage());

                log.error("Payment failed: {} - {}", payment.getTransactionId(), result.getErrorMessage());
            }

            payment = paymentLogRepository.save(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment = paymentLogRepository.save(payment);

            log.error("Payment processing error: {}", e.getMessage());
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }

        return dtoMapper.toPaymentResponse(payment);
    }

    /**
     * Get payment by ID.
     */
    public PaymentResponse getPaymentById(String id) {
        PaymentLog payment = findPaymentById(id);
        return dtoMapper.toPaymentResponse(payment);
    }

    /**
     * Get user's payments.
     */
    public List<PaymentResponse> getUserPayments(String userId) {
        return paymentLogRepository.findByUserId(userId)
                .stream()
                .map(dtoMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's payments with pagination.
     */
    public PageResponse<PaymentResponse> getUserPayments(String userId, Pageable pageable) {
        Page<PaymentResponse> page = paymentLogRepository.findByUserId(userId, pageable)
                .map(dtoMapper::toPaymentResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get all payments with pagination.
     */
    public PageResponse<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<PaymentResponse> page = paymentLogRepository.findAll(pageable)
                .map(dtoMapper::toPaymentResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get payments by status.
     */
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentLogRepository.findByStatus(status)
                .stream()
                .map(dtoMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Refund a payment.
     */
    public PaymentResponse refundPayment(String id, String reason) {
        PaymentLog payment = findPaymentById(id);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Can only refund successful payments");
        }

        try {
            RazorpayService.RefundResult result = razorpayService.processRefund(
                    payment.getExternalPaymentId(),
                    payment.getAmount());

            if (result.isSuccess()) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAmount(payment.getAmount());

                // Update invoice status
                if (payment.getInvoiceId() != null) {
                    invoiceRepository.findById(payment.getInvoiceId())
                            .ifPresent(invoice -> {
                                invoice.setStatus(InvoiceStatus.REFUNDED);
                                invoiceRepository.save(invoice);
                            });
                }

                log.info("Payment refunded: {}", payment.getTransactionId());
            } else {
                throw new PaymentException("Refund failed: " + result.getErrorMessage());
            }

            payment = paymentLogRepository.save(payment);

        } catch (Exception e) {
            log.error("Refund processing error: {}", e.getMessage());
            throw new PaymentException("Refund processing failed: " + e.getMessage());
        }

        return dtoMapper.toPaymentResponse(payment);
    }

    /**
     * Generate unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    /**
     * Find payment entity by ID.
     */
    public PaymentLog findPaymentById(String id) {
        return paymentLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }
}
