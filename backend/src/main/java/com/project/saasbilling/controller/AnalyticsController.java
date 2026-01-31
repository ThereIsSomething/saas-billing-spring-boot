package com.project.saasbilling.controller;

import com.project.saasbilling.dto.AnalyticsResponse.*;
import com.project.saasbilling.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Business analytics and metrics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly-revenue")
    @Operation(summary = "Get monthly revenue")
    public ResponseEntity<List<MonthlyRevenue>> getMonthlyRevenue(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyRevenue(months));
    }

    @GetMapping("/subscription-stats")
    @Operation(summary = "Get subscription statistics")
    public ResponseEntity<SubscriptionStats> getSubscriptionStats() {
        return ResponseEntity.ok(analyticsService.getSubscriptionStats());
    }

    @GetMapping("/plan-popularity")
    @Operation(summary = "Get plan popularity")
    public ResponseEntity<List<PlanPopularity>> getPlanPopularity() {
        return ResponseEntity.ok(analyticsService.getPlanPopularity());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummary> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }
}