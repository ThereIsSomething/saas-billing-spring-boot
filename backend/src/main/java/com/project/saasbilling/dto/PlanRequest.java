package com.project.saasbilling.dto;

import com.project.saasbilling.model.BillingCycle;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for plan creation/update request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 2, max = 100, message = "Plan name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    private BigDecimal price;

    @Size(min = 3, max = 3, message = "Currency must be a 3-character code")
    private String currency;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @Min(value = 0, message = "Usage limit must be non-negative")
    private Long usageLimit;

    @Min(value = 0, message = "API calls limit must be non-negative")
    private Long apiCallsLimit;

    @Min(value = 0, message = "Storage limit must be non-negative")
    private Long storageLimitMb;

    @Min(value = 1, message = "Users limit must be at least 1")
    private Integer usersLimit;

    private Boolean isFeatured;

    @Min(value = 0, message = "Trial days must be non-negative")
    private Integer trialDays;

    private List<String> features;

    private Integer sortOrder;

    private Boolean active;
}
