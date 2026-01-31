package com.project.saasbilling.service;

import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.dto.SubscriptionRequest;
import com.project.saasbilling.dto.SubscriptionResponse;
import com.project.saasbilling.exception.BadRequestException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.PlanRepository;
import com.project.saasbilling.repository.SubscriptionRepository;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for subscription management operations.
 * Updated for MongoDB's denormalized data model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final MockPaymentService mockPaymentService;
    private final DtoMapper dtoMapper;

    /**
     * Create a new subscription for a user.
     */
    public SubscriptionResponse createSubscription(String userId, SubscriptionRequest request) {
        log.info("Creating subscription for user: {} with plan: {}", userId, request.getPlanId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        // Check for existing active subscription
        if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("User already has an active subscription");
        }

        // Check if payment is required for this plan
        boolean requiresPayment = (plan.getTrialDays() == null || plan.getTrialDays() == 0)
                && plan.getPrice() != null
                && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;

        if (requiresPayment) {
            // Verify payment has been made
            if (request.getPaymentOrderId() == null || request.getPaymentOrderId().isBlank()) {
                throw new BadRequestException("Payment is required for this plan. Please complete payment first.");
            }

            if (!mockPaymentService.isPaymentVerified(request.getPaymentOrderId())) {
                throw new BadRequestException("Payment not verified. Please complete payment before subscribing.");
            }

            // Verify payment is for the correct plan
            String paidPlanId = mockPaymentService.getPlanIdForOrder(request.getPaymentOrderId());
            if (!paidPlanId.equals(request.getPlanId())) {
                throw new BadRequestException("Payment was made for a different plan.");
            }

            log.info("Payment verified for order: {}", request.getPaymentOrderId());
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, plan.getBillingCycle());
        LocalDateTime trialEndDate = plan.getTrialDays() != null && plan.getTrialDays() > 0
                ? startDate.plusDays(plan.getTrialDays())
                : null;

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planId(plan.getId())
                // Denormalized fields for easy access
                .userEmail(user.getEmail())
                .planName(plan.getName())
                .planCurrency(plan.getCurrency())
                .status(plan.getTrialDays() != null && plan.getTrialDays() > 0 ? SubscriptionStatus.TRIAL
                        : SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .trialEndDate(trialEndDate)
                .nextBillingDate(trialEndDate != null ? trialEndDate : endDate)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true)
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created with id: {}", subscription.getId());

        // Generate initial invoice for paid plans (skip for trial)
        boolean isTrial = plan.getTrialDays() != null && plan.getTrialDays() > 0;
        if (!isTrial && plan.getPrice() != null && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            invoiceService.generateInvoice(subscription, user, plan);
            log.info("Invoice generated for subscription: {}", subscription.getId());
        }

        // Send email notification
        emailService.sendSubscriptionCreatedEmail(user, subscription);

        return dtoMapper.toSubscriptionResponse(subscription, plan);
    }

    /**
     * Get subscription by ID.
     */
    public SubscriptionResponse getSubscriptionById(String id) {
        Subscription subscription = findSubscriptionById(id);
        return dtoMapper.toSubscriptionResponse(subscription);
    }

    /**
     * Get subscription by ID for a specific user.
     */
    public SubscriptionResponse getSubscriptionByIdAndUser(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        return dtoMapper.toSubscriptionResponse(subscription);
    }

    /**
     * Get user's subscriptions.
     */
    public List<SubscriptionResponse> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(dtoMapper::toSubscriptionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's active subscription (returns null if no active subscription).
     */
    public SubscriptionResponse getActiveSubscription(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.TRIAL)
                .findFirst()
                .map(dtoMapper::toSubscriptionResponse)
                .orElse(null);
    }

    /**
     * Get user's subscriptions with pagination.
     */
    public PageResponse<SubscriptionResponse> getUserSubscriptions(String userId, Pageable pageable) {
        Page<SubscriptionResponse> page = subscriptionRepository.findByUserId(userId, pageable)
                .map(dtoMapper::toSubscriptionResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get all subscriptions with pagination.
     */
    public PageResponse<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {
        Page<SubscriptionResponse> page = subscriptionRepository.findAll(pageable)
                .map(dtoMapper::toSubscriptionResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get subscriptions by status.
     */
    public List<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status)
                .stream()
                .map(dtoMapper::toSubscriptionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a subscription.
     */
    public SubscriptionResponse cancelSubscription(String id, String reason) {
        Subscription subscription = findSubscriptionById(id);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Subscription is already cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription cancelled: {}", id);

        // Fetch user for email
        User user = userRepository.findById(subscription.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendSubscriptionCancelledEmail(user, subscription);
        }

        return dtoMapper.toSubscriptionResponse(subscription);
    }

    /**
     * Upgrade/downgrade subscription to a new plan.
     */
    public SubscriptionResponse changePlan(String subscriptionId, String newPlanId) {
        Subscription subscription = findSubscriptionById(subscriptionId);
        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", newPlanId));

        if (!newPlan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        String oldPlanId = subscription.getPlanId();
        Plan oldPlan = planRepository.findById(oldPlanId).orElse(null);

        // Update subscription with new plan info
        subscription.setPlanId(newPlan.getId());
        subscription.setPlanName(newPlan.getName());
        subscription.setPlanCurrency(newPlan.getCurrency());

        // Recalculate end date based on new plan's billing cycle
        LocalDateTime newEndDate = calculateEndDate(LocalDateTime.now(), newPlan.getBillingCycle());
        subscription.setEndDate(newEndDate);
        subscription.setNextBillingDate(newEndDate);

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} changed from plan {} to plan {}",
                subscriptionId, oldPlan != null ? oldPlan.getName() : oldPlanId, newPlan.getName());

        // Generate prorated invoice if applicable
        User user = userRepository.findById(subscription.getUserId()).orElse(null);
        if (user != null && oldPlan != null) {
            invoiceService.generatePlanChangeInvoice(subscription, user, oldPlan, newPlan);
        }

        return dtoMapper.toSubscriptionResponse(subscription, newPlan);
    }

    /**
     * Toggle auto-renew setting.
     */
    public SubscriptionResponse toggleAutoRenew(String id, boolean autoRenew) {
        Subscription subscription = findSubscriptionById(id);
        subscription.setAutoRenew(autoRenew);
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} auto-renew set to: {}", id, autoRenew);
        return dtoMapper.toSubscriptionResponse(subscription);
    }

    /**
     * Renew a subscription.
     */
    public SubscriptionResponse renewSubscription(String id) {
        Subscription subscription = findSubscriptionById(id);

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Subscription is already active");
        }

        final String planId = subscription.getPlanId();
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", planId));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, plan.getBillingCycle());

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setNextBillingDate(endDate);
        subscription.setCancelledAt(null);
        subscription.setCancellationReason(null);

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription renewed: {}", id);

        // Generate renewal invoice
        User user = userRepository.findById(subscription.getUserId()).orElse(null);
        if (user != null) {
            invoiceService.generateInvoice(subscription, user, plan);
        }

        return dtoMapper.toSubscriptionResponse(subscription, plan);
    }

    /**
     * Calculate end date based on billing cycle.
     */
    private LocalDateTime calculateEndDate(LocalDateTime startDate, BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    /**
     * Find subscription entity by ID.
     */
    public Subscription findSubscriptionById(String id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    }

    /**
     * Get active subscriptions count.
     */
    public long countActiveSubscriptions() {
        return subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
    }
}
