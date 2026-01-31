package com.project.saasbilling.repository;

import com.project.saasbilling.model.Invoice;
import com.project.saasbilling.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Invoice documents.
 */
@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {

        List<Invoice> findByUserId(String userId);

        Page<Invoice> findByUserId(String userId, Pageable pageable);

        Page<Invoice> findByUserIdAndStatus(String userId, InvoiceStatus status, Pageable pageable);

        List<Invoice> findBySubscriptionId(String subscriptionId);

        List<Invoice> findByStatus(InvoiceStatus status);

        List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

        Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

        long countByCreatedAtAfter(LocalDateTime date);

        long countByStatus(InvoiceStatus status);

        List<Invoice> findByStatusAndInvoiceDateAfter(InvoiceStatus status, LocalDate date);
}
