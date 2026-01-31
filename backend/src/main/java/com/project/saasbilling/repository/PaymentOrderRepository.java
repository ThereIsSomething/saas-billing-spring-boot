package com.project.saasbilling.repository;

import com.project.saasbilling.model.PaymentOrder;
import com.project.saasbilling.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentOrder documents.
 */
@Repository
public interface PaymentOrderRepository extends MongoRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    List<PaymentOrder> findByUserId(String userId);

    List<PaymentOrder> findByUserIdAndStatus(String userId, PaymentStatus status);

    boolean existsByRazorpayOrderId(String razorpayOrderId);
}
