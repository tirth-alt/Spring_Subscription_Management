package com.example.subscription_platform.dto.subscription;

import com.example.subscription_platform.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Filter criteria for subscription queries.
 * Used to filter subscriptions by status, dates, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionFilter {

    private SubscriptionStatus status;
    private String planId;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
    private Boolean autoRenew;
}
