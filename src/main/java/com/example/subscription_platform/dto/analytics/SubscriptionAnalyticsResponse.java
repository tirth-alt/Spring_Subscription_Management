package com.example.subscription_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for subscription analytics.
 * Provides subscription counts categorized by status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionAnalyticsResponse {

    /**
     * Total number of subscriptions (all statuses).
     */
    private Long totalSubscriptions;

    /**
     * Count of currently active subscriptions.
     */
    private Long activeSubscriptions;

    /**
     * Count of cancelled subscriptions.
     */
    private Long cancelledSubscriptions;

    /**
     * Count of expired subscriptions.
     */
    private Long expiredSubscriptions;

    /**
     * Count of pending subscriptions.
     */
    private Long pendingSubscriptions;

    /**
     * Breakdown of subscription counts by status.
     * Key: Status name (ACTIVE, CANCELLED, EXPIRED, PENDING)
     * Value: Count
     */
    private Map<String, Long> statusBreakdown;

    /**
     * Active subscription rate (active / total * 100).
     */
    private Double activeRate;

    /**
     * Churn rate (cancelled / total * 100).
     */
    private Double churnRate;
}
