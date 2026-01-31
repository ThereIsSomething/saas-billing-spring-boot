package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Plan document representing a subscription plan.
 */
@Document(collection = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private BigDecimal price;

    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    private Long usageLimit;

    private Long apiCallsLimit;

    private Long storageLimitMb;

    private Integer usersLimit;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Integer trialDays = 0;

    private List<String> features;

    @Builder.Default
    private Integer sortOrder = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
