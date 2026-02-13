package com.example.subscription_platform.model;

/**
 * Enum representing the lifecycle states of a subscription.
 * - PENDING: Payment pending or awaiting activation
 * - ACTIVE: Currently active subscription
 * - CANCELLED: User cancelled, may still be active until end date
 * - EXPIRED: Subscription period ended
 */
public enum SubscriptionStatus {
    PENDING,
    ACTIVE,
    CANCELLED,
    EXPIRED
}
