package com.project.saasbilling.service;

import com.project.saasbilling.dto.AnalyticsResponse.*;
import com.project.saasbilling.model.InvoiceStatus;
import com.project.saasbilling.model.Subscription;
import com.project.saasbilling.model.SubscriptionStatus;
import com.project.saasbilling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service updated for MongoDB.
 * Uses in-memory aggregation since MongoDB doesn't support the same JPQL
 * queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

        private final SubscriptionRepository subscriptionRepository;
        private final InvoiceRepository invoiceRepository;
        private final UserRepository userRepository;

        @Cacheable(value = "analytics", key = "'monthly-revenue-' + #months")
        public List<MonthlyRevenue> getMonthlyRevenue(int months) {
                LocalDate startDate = LocalDate.now().minusMonths(months);

                // Fetch invoices and aggregate in-memory for MongoDB
                var invoices = invoiceRepository.findByStatusAndInvoiceDateAfter(
                                InvoiceStatus.PAID, startDate);

                Map<String, List<com.project.saasbilling.model.Invoice>> byMonthYear = invoices.stream()
                                .collect(Collectors.groupingBy(inv -> inv.getInvoiceDate().getYear() + "-"
                                                + inv.getInvoiceDate().getMonthValue()));

                List<MonthlyRevenue> revenues = new ArrayList<>();
                for (var entry : byMonthYear.entrySet()) {
                        String[] parts = entry.getKey().split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);

                        BigDecimal totalRevenue = entry.getValue().stream()
                                        .map(inv -> inv.getTotalAmount())
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        revenues.add(MonthlyRevenue.builder()
                                        .year(year)
                                        .month(month)
                                        .revenue(totalRevenue)
                                        .invoiceCount((long) entry.getValue().size())
                                        .currency("INR")
                                        .build());
                }

                // Sort by year and month
                revenues.sort((a, b) -> {
                        int yearCmp = Integer.compare(a.getYear(), b.getYear());
                        return yearCmp != 0 ? yearCmp : Integer.compare(a.getMonth(), b.getMonth());
                });

                return revenues;
        }

        @Cacheable(value = "analytics", key = "'subscription-stats'")
        public SubscriptionStats getSubscriptionStats() {
                long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
                long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
                long expired = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
                long trial = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
                long total = active + cancelled + expired + trial;

                BigDecimal churnRate = total > 0 ? BigDecimal.valueOf(cancelled)
                                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                return SubscriptionStats.builder()
                                .total(total)
                                .active(active)
                                .cancelled(cancelled)
                                .expired(expired)
                                .trial(trial)
                                .churnRate(churnRate)
                                .build();
        }

        @Cacheable(value = "analytics", key = "'plan-popularity'")
        public List<PlanPopularity> getPlanPopularity() {
                // Aggregate plan popularity in-memory for MongoDB
                List<Subscription> allSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

                Map<String, Long> planCounts = allSubscriptions.stream()
                                .filter(s -> s.getPlanId() != null)
                                .collect(Collectors.groupingBy(Subscription::getPlanId, Collectors.counting()));

                Map<String, String> planNames = allSubscriptions.stream()
                                .filter(s -> s.getPlanId() != null && s.getPlanName() != null)
                                .collect(Collectors.toMap(
                                                Subscription::getPlanId,
                                                Subscription::getPlanName,
                                                (existing, replacement) -> existing));

                long total = planCounts.values().stream().mapToLong(Long::longValue).sum();

                List<PlanPopularity> list = new ArrayList<>();
                for (var entry : planCounts.entrySet()) {
                        long count = entry.getValue();
                        BigDecimal pct = total > 0 ? BigDecimal.valueOf(count)
                                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                        list.add(PlanPopularity.builder()
                                        .planId(entry.getKey())
                                        .planName(planNames.getOrDefault(entry.getKey(), "Unknown"))
                                        .subscriptionCount(count)
                                        .percentage(pct)
                                        .build());
                }

                // Sort by subscription count descending
                list.sort((a, b) -> Long.compare(b.getSubscriptionCount(), a.getSubscriptionCount()));

                return list;
        }

        public DashboardSummary getDashboardSummary() {
                SubscriptionStats stats = getSubscriptionStats();
                List<PlanPopularity> topPlans = getPlanPopularity().stream().limit(5).toList();

                // Calculate MRR from paid invoices this month
                LocalDate now = LocalDate.now();
                var paidInvoices = invoiceRepository.findByStatusAndInvoiceDateAfter(
                                InvoiceStatus.PAID, now.withDayOfMonth(1));

                BigDecimal mrr = paidInvoices.stream()
                                .map(inv -> inv.getTotalAmount())
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long activeUsers = userRepository.countByActiveTrue();
                BigDecimal arpu = activeUsers > 0 ? mrr.divide(
                                BigDecimal.valueOf(activeUsers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                Map<String, Long> byStatus = new HashMap<>();
                byStatus.put("ACTIVE", stats.getActive());
                byStatus.put("CANCELLED", stats.getCancelled());
                byStatus.put("TRIAL", stats.getTrial());

                return DashboardSummary.builder()
                                .monthlyRecurringRevenue(mrr)
                                .activeSubscriptions(stats.getActive())
                                .churnRate(stats.getChurnRate())
                                .averageRevenuePerUser(arpu)
                                .subscriptionsByStatus(byStatus)
                                .topPlans(topPlans)
                                .build();
        }
}
