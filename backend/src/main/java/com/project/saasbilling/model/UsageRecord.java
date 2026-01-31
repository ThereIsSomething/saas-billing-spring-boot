package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Usage record document representing resource consumption.
 */
@Document(collection = "usage_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageRecord {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String subscriptionId;

    // Denormalized for easy access
    private String userEmail;
    private String planName;

    @Indexed
    private String metricName;

    private Long usageValue;

    private String unit;

    private String description;

    @Indexed
    private LocalDateTime recordedAt;
}
