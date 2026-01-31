package com.project.saasbilling.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock Razorpay payment gateway service.
 * In production, this would integrate with actual Razorpay APIs.
 */
@Service
@Slf4j
public class RazorpayService {

    @Value("${external.razorpay.key-id}")
    private String keyId;

    @Value("${external.razorpay.key-secret}")
    private String keySecret;

    @Value("${external.razorpay.api-url}")
    private String apiUrl;

    private final Random random = new Random();

    /**
     * Process a payment through Razorpay (mock implementation).
     */
    public PaymentResult processPayment(String orderId, BigDecimal amount, String currency) {
        log.info("Processing Razorpay payment - Order: {}, Amount: {} {}", orderId, amount, currency);

        // Simulate API call delay
        simulateApiCall();

        // Simulate 95% success rate
        boolean success = random.nextInt(100) < 95;

        if (success) {
            String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 14);
            log.info("Razorpay payment successful - Payment ID: {}", paymentId);

            return PaymentResult.builder()
                    .success(true)
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .amount(amount)
                    .currency(currency)
                    .response("{\"status\":\"captured\",\"payment_id\":\"" + paymentId + "\"}")
                    .build();
        } else {
            String errorCode = "PAYMENT_FAILED";
            String errorMessage = "Payment declined by bank";
            log.error("Razorpay payment failed - Order: {}, Error: {}", orderId, errorMessage);

            return PaymentResult.builder()
                    .success(false)
                    .orderId(orderId)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .response("{\"status\":\"failed\",\"error\":{\"code\":\"" + errorCode +
                            "\",\"description\":\"" + errorMessage + "\"}}")
                    .build();
        }
    }

    /**
     * Process a refund through Razorpay (mock implementation).
     */
    public RefundResult processRefund(String paymentId, BigDecimal amount) {
        log.info("Processing Razorpay refund - Payment: {}, Amount: {}", paymentId, amount);

        // Simulate API call delay
        simulateApiCall();

        // Simulate 98% success rate for refunds
        boolean success = random.nextInt(100) < 98;

        if (success) {
            String refundId = "rfnd_" + UUID.randomUUID().toString().substring(0, 14);
            log.info("Razorpay refund successful - Refund ID: {}", refundId);

            return RefundResult.builder()
                    .success(true)
                    .refundId(refundId)
                    .paymentId(paymentId)
                    .amount(amount)
                    .response("{\"status\":\"processed\",\"refund_id\":\"" + refundId + "\"}")
                    .build();
        } else {
            String errorMessage = "Refund processing failed";
            log.error("Razorpay refund failed - Payment: {}, Error: {}", paymentId, errorMessage);

            return RefundResult.builder()
                    .success(false)
                    .paymentId(paymentId)
                    .errorMessage(errorMessage)
                    .response("{\"status\":\"failed\",\"error\":\"" + errorMessage + "\"}")
                    .build();
        }
    }

    /**
     * Create a Razorpay order (mock implementation).
     */
    public OrderResult createOrder(BigDecimal amount, String currency, String receipt) {
        log.info("Creating Razorpay order - Amount: {} {}, Receipt: {}", amount, currency, receipt);

        simulateApiCall();

        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 14);

        return OrderResult.builder()
                .success(true)
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .receipt(receipt)
                .response("{\"id\":\"" + orderId + "\",\"status\":\"created\"}")
                .build();
    }

    /**
     * Verify payment signature (mock implementation).
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        log.info("Verifying Razorpay signature - Order: {}, Payment: {}", orderId, paymentId);
        // In production, this would verify the HMAC signature
        return true;
    }

    /**
     * Simulate API call with random delay.
     */
    private void simulateApiCall() {
        try {
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String paymentId;
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String errorCode;
        private String errorMessage;
        private String response;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResult {
        private boolean success;
        private String refundId;
        private String paymentId;
        private BigDecimal amount;
        private String errorMessage;
        private String response;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResult {
        private boolean success;
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String receipt;
        private String response;
    }
}
