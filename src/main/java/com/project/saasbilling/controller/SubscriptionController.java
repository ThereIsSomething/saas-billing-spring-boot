package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.SubscriptionStatus;
import com.project.saasbilling.model.User;
import com.project.saasbilling.service.SubscriptionService;
import com.project.saasbilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubscriptionRequest request) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.createSubscription(user.getId(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(user.getId()));
    }

    @GetMapping("/my/active")
    @Operation(summary = "Get current user's active subscription")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable String id) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.getSubscriptionByIdAndUser(id, user.getId()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable String id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id, reason));
    }

    @PostMapping("/{id}/change-plan")
    @Operation(summary = "Change subscription plan")
    public ResponseEntity<SubscriptionResponse> changePlan(
            @PathVariable String id, @RequestParam String newPlanId) {
        return ResponseEntity.ok(subscriptionService.changePlan(id, newPlanId));
    }

    @PostMapping("/{id}/auto-renew")
    @Operation(summary = "Toggle auto-renewal for a subscription")
    public ResponseEntity<SubscriptionResponse> toggleAutoRenew(
            @PathVariable String id, @RequestParam boolean autoRenew) {
        return ResponseEntity.ok(subscriptionService.toggleAutoRenew(id, autoRenew));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all subscriptions (Admin)")
    public ResponseEntity<PageResponse<SubscriptionResponse>> getAllSubscriptions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions(pageable));
    }
}
