package com.project.saasbilling.util;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping documents to DTOs.
 * Updated for MongoDB's denormalized data model.
 */
@Component
@RequiredArgsConstructor
public class DtoMapper {

    private final PlanRepository planRepository;

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .company(user.getCompany())
                .role(user.getRole())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public PlanResponse toPlanResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .usageLimit(plan.getUsageLimit())
                .apiCallsLimit(plan.getApiCallsLimit())
                .storageLimitMb(plan.getStorageLimitMb())
                .usersLimit(plan.getUsersLimit())
                .active(plan.getActive())
                .isFeatured(plan.getIsFeatured())
                .trialDays(plan.getTrialDays())
                .features(plan.getFeatures())
                .sortOrder(plan.getSortOrder())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    public SubscriptionResponse toSubscriptionResponse(Subscription subscription, Plan plan) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .userEmail(subscription.getUserEmail())
                .userName(null) // Can be fetched separately if needed
                .plan(plan != null ? toPlanResponse(plan) : null)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .trialEndDate(subscription.getTrialEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .autoRenew(subscription.getAutoRenew())
                .cancelledAt(subscription.getCancelledAt())
                .cancellationReason(subscription.getCancellationReason())
                .externalSubscriptionId(subscription.getExternalSubscriptionId())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    // Overloaded method without plan lookup
    public SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        // Fetch plan if planId is available
        Plan plan = null;
        if (subscription.getPlanId() != null) {
            plan = planRepository.findById(subscription.getPlanId()).orElse(null);
        }
        return toSubscriptionResponse(subscription, plan);
    }

    public InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .userId(invoice.getUserId())
                .userEmail(invoice.getUserEmail())
                .userName(invoice.getUserName())
                .subscriptionId(invoice.getSubscriptionId())
                .planName(invoice.getPlanName())
                .amount(invoice.getAmount())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .billingPeriodStart(invoice.getBillingPeriodStart())
                .billingPeriodEnd(invoice.getBillingPeriodEnd())
                .notes(invoice.getNotes())
                .pdfUrl(invoice.getPdfUrl())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    public PaymentResponse toPaymentResponse(PaymentLog payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .invoiceId(payment.getInvoiceId())
                .invoiceNumber(payment.getInvoiceNumber())
                .transactionId(payment.getTransactionId())
                .externalPaymentId(payment.getExternalPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentGateway(payment.getPaymentGateway())
                .failureReason(payment.getFailureReason())
                .refundedAmount(payment.getRefundedAmount())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    public UsageResponse toUsageResponse(UsageRecord usage) {
        return UsageResponse.builder()
                .id(usage.getId())
                .userId(usage.getUserId())
                .userEmail(usage.getUserEmail())
                .subscriptionId(usage.getSubscriptionId())
                .planName(usage.getPlanName())
                .metricName(usage.getMetricName())
                .usageValue(usage.getUsageValue())
                .unit(usage.getUnit())
                .description(usage.getDescription())
                .recordedAt(usage.getRecordedAt())
                .build();
    }

    public FileResponse toFileResponse(UploadedFile file) {
        return FileResponse.builder()
                .id(file.getId())
                .userId(file.getUserId())
                .fileName(file.getFileName())
                .originalFileName(file.getOriginalFileName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .storageType(file.getStorageType())
                .publicUrl(file.getPublicUrl())
                .createdAt(file.getCreatedAt())
                .build();
    }

    public <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
