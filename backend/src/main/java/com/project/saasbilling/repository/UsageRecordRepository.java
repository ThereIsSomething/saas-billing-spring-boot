package com.project.saasbilling.repository;

import com.project.saasbilling.model.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for UsageRecord documents.
 */
@Repository
public interface UsageRecordRepository extends MongoRepository<UsageRecord, String> {

        List<UsageRecord> findByUserId(String userId);

        Page<UsageRecord> findByUserId(String userId, Pageable pageable);

        List<UsageRecord> findBySubscriptionId(String subscriptionId);

        List<UsageRecord> findByUserIdAndMetricName(String userId, String metricName);

        List<UsageRecord> findByUserIdAndRecordedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

        List<UsageRecord> findBySubscriptionIdAndRecordedAtBetween(String subscriptionId, LocalDateTime start,
                        LocalDateTime end);
}
