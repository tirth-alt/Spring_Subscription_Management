package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.dto.subscription.SubscriptionRequest;
import com.example.subscription_platform.dto.subscription.SubscriptionResponse;
import com.example.subscription_platform.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for subscription management.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Subscriptions", description = "Subscription management APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Subscribe to a plan.
     * 
     * @param request Subscription request
     * @return Created subscription
     */
    @PostMapping
    @Operation(summary = "Subscribe to plan", description = "Create new subscription for authenticated user")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully. Please complete payment to activate.",
                        subscription));
    }

    /**
     * Get active subscription for current user.
     * 
     * @return Active subscription or null
     */
    @GetMapping("/active")
    @Operation(summary = "Get active subscription", description = "Get current user's active subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActiveSubscription() {
        SubscriptionResponse subscription = subscriptionService.getActiveSubscription();
        if (subscription == null) {
            return ResponseEntity.ok(ApiResponse.success("No active subscription found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    /**
     * Get subscription history for current user.
     * 
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return List of subscriptions
     */
    @GetMapping("/history")
    @Operation(summary = "Get subscription history", description = "Get paginated subscription history for current user")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    /**
     * Filter subscriptions with criteria.
     * 
     * @param filter Filter criteria
     * @param page   Page number
     * @param size   Page size
     * @return Filtered subscriptions
     */
    @PostMapping("/filter")
    @Operation(summary = "Filter subscriptions", description = "Filter user subscriptions by status, plan, dates, etc.")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> filterSubscriptions(
            @Valid @RequestBody com.example.subscription_platform.dto.subscription.SubscriptionFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<SubscriptionResponse> subscriptions = subscriptionService.filterSubscriptions(filter, page, size);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    /**
     * Cancel subscription.
     * 
     * @param id Subscription ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel subscription", description = "Cancel user's subscription (soft cancel - active until end date)")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@PathVariable String id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    /**
     * Get subscription statistics for admin.
     * 
     * @return Subscription stats
     */
    @GetMapping("/admin/stats")
    @Operation(summary = "Get subscription stats", description = "Get subscription statistics for admin dashboard")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getSubscriptionStats() {
        java.util.Map<String, Long> stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
