package com.project.saasbilling.service;

import com.project.saasbilling.dto.InvoiceResponse;
import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.exception.BadRequestException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.InvoiceRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for invoice management operations.
 * Updated for MongoDB's denormalized data model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;
    private final DtoMapper dtoMapper;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    /**
     * Generate invoice for a subscription.
     */
    public Invoice generateInvoice(Subscription subscription, User user, Plan plan) {
        log.info("Generating invoice for subscription: {}", subscription.getId());

        BigDecimal amount = plan.getPrice();
        BigDecimal taxAmount = amount.multiply(TAX_RATE);
        BigDecimal totalAmount = amount.add(taxAmount);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                // Denormalized fields
                .userEmail(user.getEmail())
                .userName(user.getFullName())
                .planName(plan.getName())
                .amount(amount)
                .taxAmount(taxAmount)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .currency(plan.getCurrency())
                .status(InvoiceStatus.PENDING)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .billingPeriodStart(subscription.getStartDate() != null ? subscription.getStartDate().toLocalDate()
                        : LocalDate.now())
                .billingPeriodEnd(subscription.getEndDate() != null ? subscription.getEndDate().toLocalDate()
                        : LocalDate.now().plusMonths(1))
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice generated: {}", invoice.getInvoiceNumber());

        emailService.sendInvoiceEmail(user, invoice);

        return invoice;
    }

    /**
     * Generate prorated invoice for plan change.
     */
    public Invoice generatePlanChangeInvoice(Subscription subscription, User user, Plan oldPlan, Plan newPlan) {
        log.info("Generating plan change invoice for subscription: {}", subscription.getId());

        BigDecimal priceDiff = newPlan.getPrice().subtract(oldPlan.getPrice());

        if (priceDiff.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No invoice needed for downgrade");
            return null;
        }

        BigDecimal taxAmount = priceDiff.multiply(TAX_RATE);
        BigDecimal totalAmount = priceDiff.add(taxAmount);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                // Denormalized fields
                .userEmail(user.getEmail())
                .userName(user.getFullName())
                .planName(newPlan.getName())
                .amount(priceDiff)
                .taxAmount(taxAmount)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .currency(newPlan.getCurrency())
                .status(InvoiceStatus.PENDING)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .notes("Prorated charge for plan upgrade from " + oldPlan.getName() + " to " + newPlan.getName())
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Plan change invoice generated: {}", invoice.getInvoiceNumber());

        return invoice;
    }

    /**
     * Get invoice by ID.
     */
    public InvoiceResponse getInvoiceById(String id) {
        Invoice invoice = findInvoiceById(id);
        return dtoMapper.toInvoiceResponse(invoice);
    }

    /**
     * Get invoice by ID for a specific user.
     */
    public InvoiceResponse getInvoiceByIdAndUser(String id, String userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return dtoMapper.toInvoiceResponse(invoice);
    }

    /**
     * Get user's invoices.
     */
    public List<InvoiceResponse> getUserInvoices(String userId) {
        return invoiceRepository.findByUserId(userId)
                .stream()
                .map(dtoMapper::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's invoices with pagination.
     */
    public PageResponse<InvoiceResponse> getUserInvoices(String userId, Pageable pageable) {
        Page<InvoiceResponse> page = invoiceRepository.findByUserId(userId, pageable)
                .map(dtoMapper::toInvoiceResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get all invoices with pagination.
     */
    public PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable) {
        Page<InvoiceResponse> page = invoiceRepository.findAll(pageable)
                .map(dtoMapper::toInvoiceResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get invoices by status.
     */
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status)
                .stream()
                .map(dtoMapper::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark invoice as paid.
     */
    public InvoiceResponse markAsPaid(String id) {
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice marked as paid: {}", invoice.getInvoiceNumber());

        return dtoMapper.toInvoiceResponse(invoice);
    }

    /**
     * Cancel an invoice.
     */
    public InvoiceResponse cancelInvoice(String id) {
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice cancelled: {}", invoice.getInvoiceNumber());

        return dtoMapper.toInvoiceResponse(invoice);
    }

    /**
     * Get overdue invoices and mark them.
     */
    public List<Invoice> processOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusAndDueDateBefore(
                InvoiceStatus.PENDING, LocalDate.now());

        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            log.info("Invoice marked as overdue: {}", invoice.getInvoiceNumber());
        }

        return overdueInvoices;
    }

    /**
     * Generate unique invoice number.
     */
    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        // For MongoDB, use a UUID suffix to ensure uniqueness
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Find invoice entity by ID.
     */
    public Invoice findInvoiceById(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    /**
     * Find invoice by invoice number.
     */
    public Invoice findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
    }
}
