package com.project.saasbilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for usage record response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String subscriptionId;
    private String planName;
    private String metricName;
    private Long usageValue;
    private String unit;
    private String description;
    private LocalDateTime recordedAt;
}
