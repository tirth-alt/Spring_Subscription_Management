package com.example.subscription_platform.dto.plan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating or updating subscription plans.
 * Admin-only operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;

    private List<String> features;

    private Boolean active;
}
