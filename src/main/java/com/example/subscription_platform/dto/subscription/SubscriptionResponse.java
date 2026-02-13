package com.example.subscription_platform.dto.subscription;

import com.example.subscription_platform.dto.plan.PlanResponse;
import com.example.subscription_platform.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription information.
 * Includes embedded plan details for convenience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private String id;
    private String userId;
    private PlanResponse plan; // Embedded plan details
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
}
