package com.example.subscription_platform.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionPlan entity representing a subscription plan.
 * Plans can be created and managed by administrators.
 * Example: Basic (₹99/month), Premium (₹499/month), Enterprise (₹999/month)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscription_plans")
public class SubscriptionPlan {

    @Id
    private String id;

    @NotBlank(message = "Plan name is required")
    @Indexed(unique = true)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;

    @Builder.Default
    private List<String> features = new ArrayList<>();

    @Indexed
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
