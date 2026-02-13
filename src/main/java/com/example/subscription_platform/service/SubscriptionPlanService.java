package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.common.PagedResponse;
import com.example.subscription_platform.dto.plan.PlanRequest;
import com.example.subscription_platform.dto.plan.PlanResponse;
import com.example.subscription_platform.exception.DuplicateResourceException;
import com.example.subscription_platform.exception.ResourceNotFoundException;
import com.example.subscription_platform.model.SubscriptionPlan;
import com.example.subscription_platform.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for subscription plan management.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    /**
     * Create a new subscription plan (admin only).
     * 
     * @param request Plan details
     * @return Created plan
     */
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponse createPlan(PlanRequest request) {
        // Check for duplicate name
        if (planRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Plan", "name", request.getName());
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationInDays(request.getDurationInDays())
                .features(request.getFeatures())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        planRepository.save(plan);

        return mapToResponse(plan);
    }

    /**
     * Update subscription plan (admin only).
     * 
     * @param id      Plan ID
     * @param request Updated plan details
     * @return Updated plan
     */
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponse updatePlan(String id, PlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));

        // Check for duplicate name (if name is being changed)
        if (!plan.getName().equals(request.getName()) && planRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Plan", "name", request.getName());
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setDurationInDays(request.getDurationInDays());
        plan.setFeatures(request.getFeatures());
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }
        plan.setUpdatedAt(LocalDateTime.now());

        planRepository.save(plan);

        return mapToResponse(plan);
    }

    /**
     * Delete (deactivate) subscription plan (admin only).
     * 
     * @param id Plan ID
     */
    @CacheEvict(value = "plans", allEntries = true)
    public void deletePlan(String id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));

        // Soft delete by deactivating
        plan.setActive(false);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
    }

    /**
     * Get plan by ID.
     * 
     * @param id Plan ID
     * @return Plan details
     */
    public PlanResponse getPlanById(String id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));
        return mapToResponse(plan);
    }

    /**
     * Get all active plans.
     * 
     * @return List of active plans
     */
    @Cacheable("plans")
    public List<PlanResponse> getActivePlans() {
        return planRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active plans.
     * 
     * @param page          Page number (0-indexed)
     * @param size          Page size
     * @param sortBy        Sort field
     * @param sortDirection Sort direction (asc/desc)
     * @return Paged response
     */
    public PagedResponse<PlanResponse> getActivePlans(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SubscriptionPlan> planPage = planRepository.findByActiveTrue(pageable);

        List<PlanResponse> content = planPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(content, page, size, planPage.getTotalElements());
    }

    /**
     * Get all plans (including inactive, admin only).
     * 
     * @return List of all plans
     */
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to response DTO.
     */
    private PlanResponse mapToResponse(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationInDays(plan.getDurationInDays())
                .features(plan.getFeatures())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
