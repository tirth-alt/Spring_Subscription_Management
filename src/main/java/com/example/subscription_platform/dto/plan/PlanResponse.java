package com.example.subscription_platform.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for subscription plan information.
 * Public-facing plan details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationInDays;
    private List<String> features;
    private Boolean active;
    private LocalDateTime createdAt;
}
