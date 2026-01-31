package com.project.saasbilling.service;

import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.dto.UserResponse;
import com.project.saasbilling.dto.UserUpdateRequest;
import com.project.saasbilling.exception.ConflictException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.Role;
import com.project.saasbilling.model.User;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for user management operations.
 * Updated for MongoDB with String IDs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    /**
     * Get user by ID.
     */
    public UserResponse getUserById(String id) {
        User user = findUserById(id);
        return dtoMapper.toUserResponse(user);
    }

    /**
     * Get user by email.
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return dtoMapper.toUserResponse(user);
    }

    /**
     * Get current user from email.
     */
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get all users with pagination.
     */
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(dtoMapper::toUserResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Update user profile.
     */
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = findUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCompany() != null) {
            user.setCompany(request.getCompany());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", user.getId());

        return dtoMapper.toUserResponse(user);
    }

    /**
     * Change user password.
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ConflictException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    /**
     * Deactivate user account.
     */
    public void deactivateUser(String id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", id);
    }

    /**
     * Activate user account.
     */
    public void activateUser(String id) {
        User user = findUserById(id);
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", id);
    }

    /**
     * Toggle user active status and return updated user.
     */
    public UserResponse toggleUserActive(String id) {
        User user = findUserById(id);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User saved = userRepository.save(user);
        log.info("User {} toggled to active={}", id, saved.getActive());
        return dtoMapper.toUserResponse(saved);
    }

    /**
     * Update user role (admin only).
     */
    public UserResponse updateUserRole(String id, Role role) {
        User user = findUserById(id);
        user.setRole(role);
        user = userRepository.save(user);
        log.info("User role updated to {} for user: {}", role, id);
        return dtoMapper.toUserResponse(user);
    }

    /**
     * Get user entity by ID.
     */
    public User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Count total users.
     */
    public long countUsers() {
        return userRepository.count();
    }
}
