package com.project.saasbilling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for usage record creation request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRequest {

    private String subscriptionId;

    @NotBlank(message = "Metric name is required")
    private String metricName;

    @NotNull(message = "Usage value is required")
    @Min(value = 0, message = "Usage value must be non-negative")
    private Long usageValue;

    private String unit;

    private String description;
}
