package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.Invoice;
import com.project.saasbilling.model.InvoiceStatus;
import com.project.saasbilling.model.Plan;
import com.project.saasbilling.model.Subscription;
import com.project.saasbilling.model.User;
import com.project.saasbilling.repository.PlanRepository;
import com.project.saasbilling.repository.SubscriptionRepository;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.service.InvoiceService;
import com.project.saasbilling.service.UserService;
import com.project.saasbilling.util.DtoMapper;
import com.project.saasbilling.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final DtoMapper dtoMapper;

    @GetMapping("/my")
    @Operation(summary = "Get current user's invoices")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(invoiceService.getUserInvoices(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable String id) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(invoiceService.getInvoiceByIdAndUser(id, user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all invoices (Admin)")
    public ResponseEntity<PageResponse<InvoiceResponse>> getAllInvoices(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark invoice as paid (Admin)")
    public ResponseEntity<InvoiceResponse> markAsPaid(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel invoice (Admin)")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate invoice for a subscription (Admin)")
    public ResponseEntity<InvoiceResponse> generateInvoice(@RequestParam String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        User user = userRepository.findById(subscription.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", subscription.getUserId()));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", subscription.getPlanId()));

        Invoice invoice = invoiceService.generateInvoice(subscription, user, plan);
        return ResponseEntity.ok(dtoMapper.toInvoiceResponse(invoice));
    }
}
