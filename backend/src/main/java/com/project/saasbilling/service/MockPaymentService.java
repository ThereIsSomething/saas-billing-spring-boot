package com.project.saasbilling.service;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.exception.BadRequestException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.*;
import com.project.saasbilling.repository.PaymentOrderRepository;
import com.project.saasbilling.repository.PlanRepository;
import com.project.saasbilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Mock payment service simulating Razorpay-like payment processing.
 * For development/demo purposes only - NOT for production use.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MockPaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    // Mock Razorpay key (for demo purposes)
    private static final String MOCK_KEY_ID = "rzp_test_mock_key_123";
    private static final String MOCK_KEY_SECRET = "mock_secret_key_456";

    /**
     * Initiate a payment order for a plan subscription.
     * Returns payment details if payment is required, or indicates if plan has free
     * trial.
     */
    public PaymentInitiateResponse initiatePayment(String userId, PaymentInitiateRequest request) {
        log.info("Initiating payment for user: {} plan: {}", userId, request.getPlanId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        // Check if payment is required
        boolean requiresPayment = plan.getTrialDays() == null || plan.getTrialDays() == 0;
        requiresPayment = requiresPayment && plan.getPrice().compareTo(BigDecimal.ZERO) > 0;

        if (!requiresPayment) {
            log.info("Payment not required for plan: {} (has trial or is free)", plan.getName());
            return PaymentInitiateResponse.builder()
                    .requiresPayment(false)
                    .planName(plan.getName())
                    .message("Plan has a free trial or is free. No payment required.")
                    .build();
        }

        // Create payment order
        String razorpayOrderId = generateOrderId();

        PaymentOrder order = PaymentOrder.builder()
                .userId(userId)
                .planId(plan.getId())
                .amount(plan.getPrice())
                .currency(plan.getCurrency() != null ? plan.getCurrency() : "INR")
                .status(PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrderId)
                .userEmail(user.getEmail())
                .planName(plan.getName())
                .build();

        order = paymentOrderRepository.save(order);
        log.info("Payment order created: {}", order.getRazorpayOrderId());

        return PaymentInitiateResponse.builder()
                .orderId(razorpayOrderId)
                .amount(plan.getPrice())
                .currency(order.getCurrency())
                .planName(plan.getName())
                .keyId(MOCK_KEY_ID)
                .requiresPayment(true)
                .message("Please complete payment to activate subscription")
                .build();
    }

    /**
     * Verify a completed payment (mock verification).
     * In real Razorpay, this would verify the signature using HMAC-SHA256.
     */
    public PaymentOrderResponse verifyPayment(PaymentVerifyRequest request) {
        log.info("Verifying payment for order: {}", request.getOrderId());

        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "orderId", request.getOrderId()));

        if (order.getStatus() == PaymentStatus.SUCCESS) {
            return buildResponse(order, true, "Payment already verified");
        }

        // Mock signature verification (in production, use HMAC-SHA256)
        boolean isValid = verifySignature(request.getOrderId(), request.getPaymentId(), request.getSignature());

        if (!isValid) {
            order.setStatus(PaymentStatus.FAILED);
            order.setFailureReason("Invalid payment signature");
            paymentOrderRepository.save(order);
            log.warn("Payment verification failed for order: {}", request.getOrderId());
            return buildResponse(order, false, "Payment verification failed: Invalid signature");
        }

        // Update order with payment details
        order.setRazorpayPaymentId(request.getPaymentId());
        order.setRazorpaySignature(request.getSignature());
        order.setStatus(PaymentStatus.SUCCESS);
        order = paymentOrderRepository.save(order);

        log.info("Payment verified successfully for order: {}", order.getRazorpayOrderId());
        return buildResponse(order, true, "Payment verified successfully");
    }

    /**
     * Get payment order by Razorpay order ID.
     */
    public PaymentOrderResponse getPaymentStatus(String orderId) {
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "orderId", orderId));

        return buildResponse(order, order.getStatus() == PaymentStatus.SUCCESS, null);
    }

    /**
     * Check if a payment order is verified and can be used for subscription.
     */
    public boolean isPaymentVerified(String orderId) {
        return paymentOrderRepository.findByRazorpayOrderId(orderId)
                .map(order -> order.getStatus() == PaymentStatus.SUCCESS)
                .orElse(false);
    }

    /**
     * Get the plan ID associated with a payment order.
     */
    public String getPlanIdForOrder(String orderId) {
        return paymentOrderRepository.findByRazorpayOrderId(orderId)
                .map(PaymentOrder::getPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "orderId", orderId));
    }

    // Generate a mock order ID (similar to Razorpay format)
    private String generateOrderId() {
        return "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }

    // Mock signature verification
    // In real implementation, this would use HMAC-SHA256 with the secret key
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        // For mock purposes, we accept any signature that matches a simple pattern
        // Real implementation: signature = HMAC-SHA256(orderId + "|" + paymentId,
        // secret_key)
        try {
            String expectedData = orderId + "|" + paymentId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((expectedData + MOCK_KEY_SECRET).getBytes());
            String expectedSignature = Base64.getEncoder().encodeToString(hash).substring(0, 32);

            // For demo: accept the signature if it matches OR if it's "mock_signature"
            return signature.equals(expectedSignature) || signature.startsWith("mock_sig_");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating signature", e);
            return signature.startsWith("mock_sig_"); // Fallback for demo
        }
    }

    private PaymentOrderResponse buildResponse(PaymentOrder order, boolean verified, String message) {
        return PaymentOrderResponse.builder()
                .id(order.getId())
                .orderId(order.getRazorpayOrderId())
                .paymentId(order.getRazorpayPaymentId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .planId(order.getPlanId())
                .planName(order.getPlanName())
                .verified(verified)
                .message(message)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
