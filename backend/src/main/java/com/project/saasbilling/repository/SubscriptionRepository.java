package com.project.saasbilling.repository;

import com.project.saasbilling.model.Subscription;
import com.project.saasbilling.model.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Subscription documents.
 */
@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

        List<Subscription> findByUserId(String userId);

        Page<Subscription> findByUserId(String userId, Pageable pageable);

        Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

        List<Subscription> findByStatus(SubscriptionStatus status);

        List<Subscription> findByStatusAndNextBillingDateBefore(SubscriptionStatus status, LocalDateTime date);

        long countByStatus(SubscriptionStatus status);

        long countByStatusAndCreatedAtAfter(SubscriptionStatus status, LocalDateTime date);

        long countByPlanId(String planId);

        boolean existsByUserIdAndStatus(String userId, SubscriptionStatus status);
}
