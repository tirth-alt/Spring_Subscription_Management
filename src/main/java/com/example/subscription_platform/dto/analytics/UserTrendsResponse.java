package com.example.subscription_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for user registration trends.
 * Shows user growth over time with monthly breakdowns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTrendsResponse {

    /**
     * Total number of registered users.
     */
    private Long totalUsers;

    /**
     * Number of users registered in the current month.
     */
    private Long newUsersThisMonth;

    /**
     * Number of users registered in the previous month.
     */
    private Long newUsersLastMonth;

    /**
     * Month-over-month user growth percentage.
     * Calculated as: ((thisMonth - lastMonth) / lastMonth) * 100
     */
    private Double growthPercentage;

    /**
     * Number of active users (users with at least one active subscription).
     */
    private Long activeUsers;

    /**
     * Monthly user registration breakdown for charts.
     */
    private List<MonthlyDataPoint> monthlyRegistrations;

    /**
     * Conversion rate (users with subscriptions / total users * 100).
     */
    private Double conversionRate;
}
