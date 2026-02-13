package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.plan.PlanResponse;
import com.example.subscription_platform.dto.subscription.SubscriptionRequest;
import com.example.subscription_platform.dto.subscription.SubscriptionResponse;
import com.example.subscription_platform.exception.ResourceNotFoundException;
import com.example.subscription_platform.model.Subscription;
import com.example.subscription_platform.model.SubscriptionPlan;
import com.example.subscription_platform.model.SubscriptionStatus;
import com.example.subscription_platform.model.User;
import com.example.subscription_platform.repository.SubscriptionPlanRepository;
import com.example.subscription_platform.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for subscription management.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserService userService;
    private final SubscriptionPlanService planService;

    /**
     * Subscribe to a plan.
     * 
     * @param request Subscription request
     * @return Created subscription
     */
    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        User currentUser = userService.getCurrentUser();

        // Get plan details
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", request.getPlanId()));

        // Check if plan is active
        if (!plan.getActive()) {
            throw new RuntimeException("Cannot subscribe to inactive plan");
        }

        // Check if user already has an active subscription for this plan
        subscriptionRepository.findByUserIdAndPlanIdAndStatus(
                currentUser.getId(),
                request.getPlanId(),
                SubscriptionStatus.ACTIVE).ifPresent(sub -> {
                    throw new RuntimeException("You already have an active subscription for this plan");
                });

        // Calculate dates
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(plan.getDurationInDays());

        // Create subscription
        Subscription subscription = Subscription.builder()
                .userId(currentUser.getId())
                .planId(plan.getId())
                .status(SubscriptionStatus.PENDING) // Will be ACTIVE after payment
                .startDate(now)
                .endDate(endDate)
                .autoRenew(request.getAutoRenew())
                .build();

        subscriptionRepository.save(subscription);

        return mapToResponse(subscription, plan);
    }

    /**
     * Get active subscription for current user.
     * 
     * @return Active subscription or null
     */
    public SubscriptionResponse getActiveSubscription() {
        User currentUser = userService.getCurrentUser();

        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserIdAndStatus(
                currentUser.getId(),
                SubscriptionStatus.ACTIVE);

        if (activeSubscriptions.isEmpty()) {
            return null;
        }

        // Return first active subscription (business rule: one active subscription at a
        // time)
        Subscription subscription = activeSubscriptions.get(0);
        SubscriptionPlan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", subscription.getPlanId()));

        return mapToResponse(subscription, plan);
    }

    /**
     * Get subscription history for current user.
     * 
     * @param page Page number
     * @param size Page size
     * @return List of subscriptions
     */
    public List<SubscriptionResponse> getSubscriptionHistory(int page, int size) {
        User currentUser = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Subscription> subscriptionPage = subscriptionRepository.findByUserId(currentUser.getId(), pageable);

        return subscriptionPage.getContent().stream()
                .map(subscription -> {
                    SubscriptionPlan plan = planRepository.findById(subscription.getPlanId())
                            .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", subscription.getPlanId()));
                    return mapToResponse(subscription, plan);
                })
                .collect(Collectors.toList());
    }

    /**
     * Cancel subscription.
     * 
     * @param subscriptionId Subscription ID
     */
    public void cancelSubscription(String subscriptionId) {
        User currentUser = userService.getCurrentUser();

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        // Verify ownership
        if (!subscription.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only cancel your own subscriptions");
        }

        // Handle PENDING subscriptions - just delete them
        if (subscription.getStatus() == SubscriptionStatus.PENDING) {
            subscriptionRepository.delete(subscription);
            return;
        }

        // Can only cancel active subscriptions
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Can only cancel active or pending subscriptions");
        }

        // Cancel subscription (soft cancel - remains active until end date)
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }

    /**
     * Activate subscription (e.g., after successful payment).
     * 
     * @param subscriptionId Subscription ID
     */
    public void activateSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }

    /**
     * Auto-expire subscriptions (to be called by scheduled task).
     */
    public void autoExpireSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now());

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
        }
    }

    /**
     * Filter subscriptions with criteria.
     * 
     * @param filter Filter criteria
     * @param page   Page number
     * @param size   Page size
     * @return Filtered subscriptions
     */
    public List<SubscriptionResponse> filterSubscriptions(
            com.example.subscription_platform.dto.subscription.SubscriptionFilter filter,
            int page,
            int size) {
        User currentUser = userService.getCurrentUser();

        // Start with user's subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(currentUser.getId());

        // Apply filters
        if (filter.getStatus() != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getStatus() == filter.getStatus())
                    .collect(Collectors.toList());
        }

        if (filter.getPlanId() != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getPlanId().equals(filter.getPlanId()))
                    .collect(Collectors.toList());
        }

        if (filter.getStartDateFrom() != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getStartDate().isAfter(filter.getStartDateFrom()))
                    .collect(Collectors.toList());
        }

        if (filter.getStartDateTo() != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getStartDate().isBefore(filter.getStartDateTo()))
                    .collect(Collectors.toList());
        }

        if (filter.getAutoRenew() != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getAutoRenew().equals(filter.getAutoRenew()))
                    .collect(Collectors.toList());
        }

        // Sort and paginate
        return subscriptions.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .skip((long) page * size)
                .limit(size)
                .map(subscription -> {
                    SubscriptionPlan plan = planRepository.findById(subscription.getPlanId())
                            .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", subscription.getPlanId()));
                    return mapToResponse(subscription, plan);
                })
                .collect(Collectors.toList());
    }

    /**
     * Map subscription and plan to response DTO.
     */
    private SubscriptionResponse mapToResponse(Subscription subscription, SubscriptionPlan plan) {
        PlanResponse planResponse = planService.getPlanById(plan.getId());

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .plan(planResponse)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .createdAt(subscription.getCreatedAt())
                .build();
    }

    /**
     * Get subscription statistics for admin dashboard.
     * 
     * @return Map containing subscription stats
     */
    public java.util.Map<String, Long> getSubscriptionStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();

        stats.put("activeSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE));
        stats.put("pendingSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.PENDING));
        stats.put("totalSubscriptions", subscriptionRepository.count());

        return stats;
    }
}
