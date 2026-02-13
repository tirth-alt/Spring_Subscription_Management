package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.analytics.*;
import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for analytics and reporting endpoints.
 * All endpoints are restricted to ADMIN role only.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Admin-only analytics and reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get aggregated dashboard summary.
     * Returns key metrics including revenue, subscriptions, and user stats.
     * 
     * @return Dashboard summary
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Dashboard Summary", description = "Returns an aggregated overview of key metrics for the admin dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        log.info("Admin requesting dashboard summary");
        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }

    /**
     * Get detailed revenue analytics.
     * 
     * @return Revenue analytics with breakdowns
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Revenue Analytics", description = "Returns detailed revenue analytics including monthly breakdowns and growth metrics")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics() {
        log.info("Admin requesting revenue analytics");
        RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Revenue analytics retrieved", analytics));
    }

    /**
     * Get revenue analytics for a specific date range.
     * 
     * @param startDate Start date (ISO format)
     * @param endDate   End date (ISO format)
     * @return Revenue analytics for the period
     */
    @GetMapping("/revenue/range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Revenue Analytics for Date Range", description = "Returns revenue analytics for a specific date range")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalyticsForRange(
            @Parameter(description = "Start date (ISO format, e.g., 2024-01-01T00:00:00)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format, e.g., 2024-12-31T23:59:59)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Admin requesting revenue analytics for range: {} to {}", startDate, endDate);
        RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Revenue analytics for range retrieved", analytics));
    }

    /**
     * Get subscription analytics with status breakdown.
     * 
     * @return Subscription analytics
     */
    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Subscription Analytics", description = "Returns subscription counts by status with churn and activity rates")
    public ResponseEntity<ApiResponse<SubscriptionAnalyticsResponse>> getSubscriptionAnalytics() {
        log.info("Admin requesting subscription analytics");
        SubscriptionAnalyticsResponse analytics = analyticsService.getSubscriptionAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Subscription analytics retrieved", analytics));
    }

    /**
     * Get popular plans ranked by active subscribers.
     * 
     * @return List of plans ranked by popularity
     */
    @GetMapping("/plans/popular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Popular Plans", description = "Returns all subscription plans ranked by number of active subscribers")
    public ResponseEntity<ApiResponse<List<PlanPopularityResponse>>> getPopularPlans() {
        log.info("Admin requesting popular plans analytics");
        List<PlanPopularityResponse> popularPlans = analyticsService.getPopularPlans();
        return ResponseEntity.ok(ApiResponse.success("Popular plans retrieved", popularPlans));
    }

    /**
     * Get user registration trends.
     * 
     * @return User trends with monthly breakdowns
     */
    @GetMapping("/users/trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get User Trends", description = "Returns user registration trends with growth metrics and monthly breakdowns")
    public ResponseEntity<ApiResponse<UserTrendsResponse>> getUserTrends() {
        log.info("Admin requesting user trends analytics");
        UserTrendsResponse trends = analyticsService.getUserTrends();
        return ResponseEntity.ok(ApiResponse.success("User trends retrieved", trends));
    }
}
