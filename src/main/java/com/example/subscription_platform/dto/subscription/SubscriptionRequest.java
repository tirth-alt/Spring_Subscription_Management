package com.example.subscription_platform.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Plan ID is required")
    private String planId;

    @Builder.Default
    private Boolean autoRenew = false;
}
