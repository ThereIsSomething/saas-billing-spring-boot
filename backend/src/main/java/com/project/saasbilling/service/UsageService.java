package com.project.saasbilling.service;

import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.dto.UsageRequest;
import com.project.saasbilling.dto.UsageResponse;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.SubscriptionRepository;
import com.project.saasbilling.repository.UsageRecordRepository;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for usage tracking.
 * Updated for MongoDB with denormalized data model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    /**
     * Record usage for a user.
     */
    public UsageResponse recordUsage(String userId, UsageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Subscription subscription = null;
        String planName = null;

        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Subscription", "id", request.getSubscriptionId()));
            planName = subscription.getPlanName();
        }

        UsageRecord usage = UsageRecord.builder()
                .userId(userId)
                .subscriptionId(request.getSubscriptionId())
                // Denormalized fields
                .userEmail(user.getEmail())
                .planName(planName)
                .metricName(request.getMetricName())
                .usageValue(request.getUsageValue())
                .unit(request.getUnit())
                .description(request.getDescription())
                .recordedAt(LocalDateTime.now())
                .build();

        usage = usageRecordRepository.save(usage);
        log.info("Usage recorded for user: {}, metric: {}, value: {}", userId, request.getMetricName(),
                request.getUsageValue());

        return dtoMapper.toUsageResponse(usage);
    }

    /**
     * Get user's usage records with pagination.
     */
    public PageResponse<UsageResponse> getUserUsage(String userId, Pageable pageable) {
        Page<UsageResponse> page = usageRecordRepository.findByUserId(userId, pageable)
                .map(dtoMapper::toUsageResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get user's usage by metric.
     */
    public List<UsageResponse> getUserUsageByMetric(String userId, String metricName) {
        return usageRecordRepository.findByUserIdAndMetricName(userId, metricName)
                .stream()
                .map(dtoMapper::toUsageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total usage for a user within a time range.
     */
    public Long getTotalUsage(String userId, String metric, LocalDateTime start, LocalDateTime end) {
        List<UsageRecord> records = usageRecordRepository.findByUserIdAndRecordedAtBetween(userId, start, end);
        return records.stream()
                .filter(r -> metric.equals(r.getMetricName()))
                .mapToLong(UsageRecord::getUsageValue)
                .sum();
    }

    /**
     * Get usage summary by metric for a user.
     */
    public Map<String, Long> getUserUsageSummary(String userId, LocalDateTime startDate) {
        List<UsageRecord> records = usageRecordRepository.findByUserIdAndRecordedAtBetween(
                userId, startDate, LocalDateTime.now());

        Map<String, Long> summary = new HashMap<>();
        for (UsageRecord record : records) {
            summary.merge(record.getMetricName(), record.getUsageValue(), Long::sum);
        }
        return summary;
    }
}
