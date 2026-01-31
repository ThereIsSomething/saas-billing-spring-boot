package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Subscription plan management")
public class PlanController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "Get all active plans")
    public ResponseEntity<List<PlanResponse>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all plans with pagination (Admin)")
    public ResponseEntity<PageResponse<PlanResponse>> getAllPlans(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planService.getAllPlans(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get plan by ID")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable String id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured plans")
    public ResponseEntity<List<PlanResponse>> getFeaturedPlans() {
        return ResponseEntity.ok(planService.getFeaturedPlans());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new plan (Admin)")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a plan (Admin)")
    public ResponseEntity<PlanResponse> updatePlan(@PathVariable String id, @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate a plan (Admin)")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String id) {
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle plan active status (Admin)")
    public ResponseEntity<PlanResponse> togglePlanActive(@PathVariable String id) {
        return ResponseEntity.ok(planService.togglePlanActive(id));
    }
}
