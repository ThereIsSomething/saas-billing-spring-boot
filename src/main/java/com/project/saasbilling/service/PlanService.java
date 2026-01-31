package com.project.saasbilling.service;

import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.dto.PlanRequest;
import com.project.saasbilling.dto.PlanResponse;
import com.project.saasbilling.exception.ConflictException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.Plan;
import com.project.saasbilling.repository.PlanRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for plan management operations.
 * Updated for MongoDB with String IDs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;
    private final DtoMapper dtoMapper;

    /**
     * Get all active plans (cached).
     */
    @Cacheable(value = "plans", key = "'active'")
    public List<PlanResponse> getActivePlans() {
        log.debug("Fetching active plans from database");
        return planRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(dtoMapper::toPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all plans with pagination.
     */
    public PageResponse<PlanResponse> getAllPlans(Pageable pageable) {
        Page<PlanResponse> page = planRepository.findAll(pageable)
                .map(dtoMapper::toPlanResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get plan by ID.
     */
    public PlanResponse getPlanById(String id) {
        Plan plan = findPlanById(id);
        return dtoMapper.toPlanResponse(plan);
    }

    /**
     * Get featured plans.
     */
    @Cacheable(value = "plans", key = "'featured'")
    public List<PlanResponse> getFeaturedPlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .filter(Plan::getIsFeatured)
                .map(dtoMapper::toPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new plan.
     */
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponse createPlan(PlanRequest request) {
        log.info("Creating new plan: {}", request.getName());

        if (planRepository.existsByName(request.getName())) {
            throw new ConflictException("Plan", "name", request.getName());
        }

        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .billingCycle(request.getBillingCycle())
                .usageLimit(request.getUsageLimit())
                .apiCallsLimit(request.getApiCallsLimit())
                .storageLimitMb(request.getStorageLimitMb())
                .usersLimit(request.getUsersLimit())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .trialDays(request.getTrialDays() != null ? request.getTrialDays() : 0)
                .features(request.getFeatures())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        plan = planRepository.save(plan);
        log.info("Plan created with id: {}", plan.getId());

        return dtoMapper.toPlanResponse(plan);
    }

    /**
     * Update an existing plan.
     */
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponse updatePlan(String id, PlanRequest request) {
        Plan plan = findPlanById(id);

        if (request.getName() != null && !request.getName().equals(plan.getName())) {
            if (planRepository.existsByName(request.getName())) {
                throw new ConflictException("Plan", "name", request.getName());
            }
            plan.setName(request.getName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getBillingCycle() != null) {
            plan.setBillingCycle(request.getBillingCycle());
        }
        if (request.getUsageLimit() != null) {
            plan.setUsageLimit(request.getUsageLimit());
        }
        if (request.getApiCallsLimit() != null) {
            plan.setApiCallsLimit(request.getApiCallsLimit());
        }
        if (request.getStorageLimitMb() != null) {
            plan.setStorageLimitMb(request.getStorageLimitMb());
        }
        if (request.getUsersLimit() != null) {
            plan.setUsersLimit(request.getUsersLimit());
        }
        if (request.getIsFeatured() != null) {
            plan.setIsFeatured(request.getIsFeatured());
        }
        if (request.getTrialDays() != null) {
            plan.setTrialDays(request.getTrialDays());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }

        plan = planRepository.save(plan);
        log.info("Plan updated: {}", id);

        return dtoMapper.toPlanResponse(plan);
    }

    /**
     * Deactivate a plan (soft delete).
     */
    @CacheEvict(value = "plans", allEntries = true)
    public void deactivatePlan(String id) {
        Plan plan = findPlanById(id);
        plan.setActive(false);
        planRepository.save(plan);
        log.info("Plan deactivated: {}", id);
    }

    /**
     * Activate a plan.
     */
    @CacheEvict(value = "plans", allEntries = true)
    public void activatePlan(String id) {
        Plan plan = findPlanById(id);
        plan.setActive(true);
        planRepository.save(plan);
        log.info("Plan activated: {}", id);
    }

    /**
     * Toggle plan active status and return updated plan.
     */
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponse togglePlanActive(String id) {
        Plan plan = findPlanById(id);
        plan.setActive(!Boolean.TRUE.equals(plan.getActive()));
        Plan saved = planRepository.save(plan);
        log.info("Plan {} toggled to active={}", id, saved.getActive());
        return dtoMapper.toPlanResponse(saved);
    }

    /**
     * Delete a plan permanently.
     */
    @CacheEvict(value = "plans", allEntries = true)
    public void deletePlan(String id) {
        Plan plan = findPlanById(id);
        planRepository.delete(plan);
        log.info("Plan deleted: {}", id);
    }

    /**
     * Get plan entity by ID.
     */
    public Plan findPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));
    }
}
