package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.PaymentStatus;
import com.project.saasbilling.model.User;
import com.project.saasbilling.service.MockPaymentService;
import com.project.saasbilling.service.PaymentService;
import com.project.saasbilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final MockPaymentService mockPaymentService;
    private final UserService userService;

    // ==================== Mock Razorpay Payment Flow ====================

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for plan subscription (Mock Razorpay)")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentInitiateRequest request) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(mockPaymentService.initiatePayment(user.getId(), request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify completed payment (Mock Razorpay)")
    public ResponseEntity<PaymentOrderResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(mockPaymentService.verifyPayment(request));
    }

    @GetMapping("/order/{orderId}/status")
    @Operation(summary = "Get payment order status")
    public ResponseEntity<PaymentOrderResponse> getPaymentOrderStatus(
            @PathVariable String orderId) {
        return ResponseEntity.ok(mockPaymentService.getPaymentStatus(orderId));
    }

    // ==================== Original Payment Endpoints ====================

    @PostMapping
    @Operation(summary = "Process a payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(paymentService.processPayment(user.getId(), request, ip));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(paymentService.getUserPayments(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments (Admin)")
    public ResponseEntity<PageResponse<PaymentResponse>> getAllPayments(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a payment (Admin)")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable String id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(paymentService.refundPayment(id, reason));
    }
}
