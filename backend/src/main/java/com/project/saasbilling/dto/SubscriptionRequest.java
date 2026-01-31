package com.project.saasbilling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for subscription creation request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Plan ID is required")
    private String planId;

    private Boolean autoRenew;

    // Optional: payment order ID for paid plans (required if plan has no trial)
    private String paymentOrderId;
}
