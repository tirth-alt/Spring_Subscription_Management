package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.dto.common.PagedResponse;
import com.example.subscription_platform.dto.plan.PlanRequest;
import com.example.subscription_platform.dto.plan.PlanResponse;
import com.example.subscription_platform.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for subscription plan management.
 * Public: viewing plans
 * Admin only: creating, updating, deleting plans
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Subscription plan management APIs")
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    /**
     * Get all active plans (public).
     * 
     * @return List of active plans
     */
    @GetMapping
    @Operation(summary = "Get all active plans", description = "Get list of all active subscription plans (Public)")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getActivePlans() {
        List<PlanResponse> plans = planService.getActivePlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * Get active plans with pagination (public).
     * 
     * @param page          Page number (default 0)
     * @param size          Page size (default 10)
     * @param sortBy        Sort field (default "price")
     * @param sortDirection Sort direction (default "asc")
     * @return Paged response
     */
    @GetMapping("/paged")
    @Operation(summary = "Get active plans with pagination", description = "Get paginated list of active plans with sorting (Public)")
    public ResponseEntity<ApiResponse<PagedResponse<PlanResponse>>> getActivePlansPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PagedResponse<PlanResponse> plans = planService.getActivePlans(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * Get plan by ID (public).
     * 
     * @param id Plan ID
     * @return Plan details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get plan by ID", description = "Get subscription plan details by ID (Public)")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanById(@PathVariable String id) {
        PlanResponse plan = planService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    /**
     * Create new plan (admin only).
     * 
     * @param request Plan details
     * @return Created plan
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create new plan", description = "Create new subscription plan (Admin only)")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(@Valid @RequestBody PlanRequest request) {
        PlanResponse plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plan created successfully", plan));
    }

    /**
     * Update plan (admin only).
     * 
     * @param id      Plan ID
     * @param request Updated plan details
     * @return Updated plan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update plan", description = "Update subscription plan (Admin only)")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody PlanRequest request) {
        PlanResponse plan = planService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plan updated successfully", plan));
    }

    /**
     * Delete plan (admin only - soft delete).
     * 
     * @param id Plan ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete plan", description = "Soft delete subscription plan (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Plan deleted successfully", null));
    }

    /**
     * Get all plans including inactive (admin only).
     * 
     * @return List of all plans
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all plans", description = "Get all plans including inactive (Admin only)")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAllPlans() {
        List<PlanResponse> plans = planService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
