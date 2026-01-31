package com.project.saasbilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for analytics response.
 */
public class AnalyticsResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private int year;
        private int month;
        private BigDecimal revenue;
        private String currency;
        private long invoiceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionStats {
        private long total;
        private long active;
        private long cancelled;
        private long expired;
        private long trial;
        private BigDecimal churnRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanPopularity {
        private String planId;
        private String planName;
        private long subscriptionCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private BigDecimal monthlyRecurringRevenue;
        private long activeSubscriptions;
        private BigDecimal churnRate;
        private BigDecimal averageRevenuePerUser;
        private Map<String, Long> subscriptionsByStatus;
        private List<PlanPopularity> topPlans;
    }
}
