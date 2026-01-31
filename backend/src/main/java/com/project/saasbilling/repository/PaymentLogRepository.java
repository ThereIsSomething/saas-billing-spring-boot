package com.project.saasbilling.repository;

import com.project.saasbilling.model.PaymentLog;
import com.project.saasbilling.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for PaymentLog documents.
 */
@Repository
public interface PaymentLogRepository extends MongoRepository<PaymentLog, String> {

        List<PaymentLog> findByUserId(String userId);

        Page<PaymentLog> findByUserId(String userId, Pageable pageable);

        List<PaymentLog> findByInvoiceId(String invoiceId);

        Optional<PaymentLog> findByTransactionId(String transactionId);

        Optional<PaymentLog> findByExternalPaymentId(String externalPaymentId);

        List<PaymentLog> findByStatus(PaymentStatus status);

        List<PaymentLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        long countByStatus(PaymentStatus status);

        long countByCreatedAtAfter(LocalDateTime date);
}
