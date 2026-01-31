package com.project.saasbilling.dto;

import com.project.saasbilling.model.BillingCycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for plan response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private BillingCycle billingCycle;
    private Long usageLimit;
    private Long apiCallsLimit;
    private Long storageLimitMb;
    private Integer usersLimit;
    private Boolean active;
    private Boolean isFeatured;
    private Integer trialDays;
    private List<String> features;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
