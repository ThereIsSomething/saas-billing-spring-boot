package com.project.saasbilling.dto;

import com.project.saasbilling.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response (without sensitive data).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String company;
    private Role role;
    private Boolean active;
    private Boolean emailVerified;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
