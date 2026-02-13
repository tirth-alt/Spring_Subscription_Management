package com.example.subscription_platform.model;

/**
 * Enum representing the status of a payment transaction.
 * - PENDING: Payment initiated but not yet completed
 * - SUCCESS: Payment completed successfully
 * - FAILED: Payment failed
 * - REFUNDED: Payment was refunded
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
