package com.project.saasbilling.dto;

import com.project.saasbilling.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subscription response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private PlanResponse plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String externalSubscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}