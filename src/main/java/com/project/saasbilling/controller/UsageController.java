package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.User;
import com.project.saasbilling.service.UsageService;
import com.project.saasbilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Tag(name = "Usage", description = "Usage tracking")
@SecurityRequirement(name = "bearerAuth")
public class UsageController {

    private final UsageService usageService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Record usage")
    public ResponseEntity<UsageResponse> recordUsage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UsageRequest request) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(usageService.recordUsage(user.getId(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's usage")
    public ResponseEntity<PageResponse<UsageResponse>> getMyUsage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 50) Pageable pageable) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(usageService.getUserUsage(user.getId(), pageable));
    }

    @GetMapping("/my/summary")
    @Operation(summary = "Get current user's usage summary")
    public ResponseEntity<Map<String, Long>> getMyUsageSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") int days) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return ResponseEntity.ok(usageService.getUserUsageSummary(user.getId(), startDate));
    }
}
