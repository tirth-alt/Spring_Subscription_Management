package com.example.subscription_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated dashboard summary response.
 * Provides a high-level overview of key metrics for the admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    /**
     * Total revenue from all successful payments.
     */
    private BigDecimal totalRevenue;

    /**
     * Revenue generated this month.
     */
    private BigDecimal revenueThisMonth;

    /**
     * Month-over-month revenue growth percentage.
     */
    private Double revenueGrowth;

    /**
     * Total number of active subscriptions.
     */
    private Long activeSubscriptions;

    /**
     * Total number of registered users.
     */
    private Long totalUsers;

    /**
     * New users registered this month.
     */
    private Long newUsersThisMonth;

    /**
     * Month-over-month user growth percentage.
     */
    private Double userGrowth;

    /**
     * Top 3 most popular subscription plans.
     */
    private List<PlanPopularityResponse> topPlans;

    /**
     * Total number of subscription plans available.
     */
    private Long totalPlans;

    /**
     * Churn rate (cancelled subscriptions / total).
     */
    private Double churnRate;

    /**
     * Average revenue per user.
     */
    private BigDecimal averageRevenuePerUser;
}
