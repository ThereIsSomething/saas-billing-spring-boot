package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

/**
 * Subscription document representing a user's subscription to a plan.
 */
@Document(collection = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String planId;

    // Denormalized for easy access
    private String userEmail;
    private String planName;
    private String planCurrency;

    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime trialEndDate;

    private LocalDateTime nextBillingDate;

    @Builder.Default
    private Boolean autoRenew = true;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    private String externalSubscriptionId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
