package com.example.subscription_platform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Subscription entity representing a user's subscription to a plan.
 * Tracks subscription status, dates, and auto-renewal settings.
 * Compound index on userId + status for efficient queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
@CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}")
public class Subscription {

    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    @Indexed
    private String userId;

    @NotBlank(message = "Plan ID is required")
    @Indexed
    private String planId;

    @NotNull(message = "Status is required")
    @Indexed
    private SubscriptionStatus status;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Indexed
    private LocalDateTime endDate;

    @Builder.Default
    private Boolean autoRenew = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Helper method to check if subscription is active
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
                LocalDateTime.now().isBefore(endDate);
    }

    // Helper method to check if subscription has expired
    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
}
