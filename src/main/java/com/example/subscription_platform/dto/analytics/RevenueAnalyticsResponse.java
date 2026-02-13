package com.example.subscription_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for revenue analytics.
 * Provides comprehensive revenue insights including totals, trends, and
 * breakdowns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {

    /**
     * Total revenue from all successful payments.
     */
    private BigDecimal totalRevenue;

    /**
     * Revenue generated in the current month.
     */
    private BigDecimal revenueThisMonth;

    /**
     * Revenue generated in the previous month.
     */
    private BigDecimal revenueLastMonth;

    /**
     * Month-over-month growth percentage.
     * Calculated as: ((thisMonth - lastMonth) / lastMonth) * 100
     */
    private Double growthPercentage;

    /**
     * Average revenue per successful payment.
     */
    private BigDecimal averagePaymentAmount;

    /**
     * Total number of successful payments.
     */
    private Long totalSuccessfulPayments;

    /**
     * Monthly revenue breakdown for charts.
     */
    private List<MonthlyDataPoint> monthlyBreakdown;

    /**
     * Currency code (default: INR).
     */
    @Builder.Default
    private String currency = "INR";
}
