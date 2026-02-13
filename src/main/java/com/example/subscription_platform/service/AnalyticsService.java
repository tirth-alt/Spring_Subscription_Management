package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.analytics.*;
import com.example.subscription_platform.model.Payment;
import com.example.subscription_platform.model.PaymentStatus;
import com.example.subscription_platform.model.SubscriptionPlan;
import com.example.subscription_platform.model.SubscriptionStatus;
import com.example.subscription_platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
// import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating analytics and reports.
 * Provides insights into revenue, subscriptions, plan popularity, and user
 * trends.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;

    /**
     * Get aggregated dashboard summary with key metrics.
     * 
     * @return Dashboard summary with revenue, subscriptions, and user stats
     */
    public DashboardSummaryResponse getDashboardSummary() {
        log.debug("Generating dashboard summary");

        // Revenue metrics
        BigDecimal totalRevenue = calculateTotalRevenue();
        BigDecimal revenueThisMonth = calculateRevenueForMonth(YearMonth.now());
        BigDecimal revenueLastMonth = calculateRevenueForMonth(YearMonth.now().minusMonths(1));
        Double revenueGrowth = calculateGrowthPercentage(revenueThisMonth, revenueLastMonth);

        // Subscription metrics
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long cancelledSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long totalSubscriptions = subscriptionRepository.count();
        Double churnRate = totalSubscriptions > 0
                ? (double) cancelledSubscriptions / totalSubscriptions * 100
                : 0.0;

        // User metrics
        long totalUsers = userRepository.count();
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

        LocalDateTime startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(23, 59, 59);
        long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double userGrowth = calculateGrowthPercentage(newUsersThisMonth, newUsersLastMonth);

        // Top plans
        List<PlanPopularityResponse> topPlans = getPopularPlans().stream()
                .limit(3)
                .collect(Collectors.toList());

        // Average revenue per user
        BigDecimal avgRevenuePerUser = totalUsers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueGrowth(revenueGrowth)
                .activeSubscriptions(activeSubscriptions)
                .totalUsers(totalUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .userGrowth(userGrowth)
                .topPlans(topPlans)
                .totalPlans(planRepository.count())
                .churnRate(churnRate)
                .averageRevenuePerUser(avgRevenuePerUser)
                .build();
    }

    /**
     * Get detailed revenue analytics.
     * 
     * @return Revenue analytics with breakdowns and trends
     */
    public RevenueAnalyticsResponse getRevenueAnalytics() {
        log.debug("Generating revenue analytics");

        BigDecimal totalRevenue = calculateTotalRevenue();
        BigDecimal revenueThisMonth = calculateRevenueForMonth(YearMonth.now());
        BigDecimal revenueLastMonth = calculateRevenueForMonth(YearMonth.now().minusMonths(1));
        Double growthPercentage = calculateGrowthPercentage(revenueThisMonth, revenueLastMonth);

        // Get successful payments for additional metrics
        List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);
        long totalSuccessfulPayments = successfulPayments.size();
        BigDecimal averagePayment = totalSuccessfulPayments > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalSuccessfulPayments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Monthly breakdown for last 12 months
        List<MonthlyDataPoint> monthlyBreakdown = getMonthlyRevenueBreakdown(12);

        return RevenueAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .growthPercentage(growthPercentage)
                .averagePaymentAmount(averagePayment)
                .totalSuccessfulPayments(totalSuccessfulPayments)
                .monthlyBreakdown(monthlyBreakdown)
                .currency("INR")
                .build();
    }

    /**
     * Get revenue analytics for a specific date range.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Revenue analytics for the period
     */
    public RevenueAnalyticsResponse getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Generating revenue analytics for date range: {} to {}", startDate, endDate);

        List<Payment> paymentsInRange = paymentRepository.findSuccessfulPaymentsInDateRange(
                PaymentStatus.SUCCESS, startDate, endDate);

        BigDecimal totalRevenue = paymentsInRange.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalSuccessfulPayments = paymentsInRange.size();
        BigDecimal averagePayment = totalSuccessfulPayments > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalSuccessfulPayments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RevenueAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .averagePaymentAmount(averagePayment)
                .totalSuccessfulPayments(totalSuccessfulPayments)
                .currency("INR")
                .build();
    }

    /**
     * Get subscription analytics with status breakdown.
     * 
     * @return Subscription analytics
     */
    public SubscriptionAnalyticsResponse getSubscriptionAnalytics() {
        log.debug("Generating subscription analytics");

        long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
        long pendingCount = subscriptionRepository.countByStatus(SubscriptionStatus.PENDING);
        long totalCount = subscriptionRepository.count();

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        statusBreakdown.put("ACTIVE", activeCount);
        statusBreakdown.put("CANCELLED", cancelledCount);
        statusBreakdown.put("EXPIRED", expiredCount);
        statusBreakdown.put("PENDING", pendingCount);

        Double activeRate = totalCount > 0 ? (double) activeCount / totalCount * 100 : 0.0;
        Double churnRate = totalCount > 0 ? (double) cancelledCount / totalCount * 100 : 0.0;

        return SubscriptionAnalyticsResponse.builder()
                .totalSubscriptions(totalCount)
                .activeSubscriptions(activeCount)
                .cancelledSubscriptions(cancelledCount)
                .expiredSubscriptions(expiredCount)
                .pendingSubscriptions(pendingCount)
                .statusBreakdown(statusBreakdown)
                .activeRate(Math.round(activeRate * 100.0) / 100.0)
                .churnRate(Math.round(churnRate * 100.0) / 100.0)
                .build();
    }

    /**
     * Get popular plans ranked by active subscribers.
     * 
     * @return List of plans ranked by popularity
     */
    public List<PlanPopularityResponse> getPopularPlans() {
        log.debug("Generating popular plans analytics");

        List<SubscriptionPlan> allPlans = planRepository.findAll();
        long totalActiveSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        List<PlanPopularityResponse> popularPlans = new ArrayList<>();

        for (SubscriptionPlan plan : allPlans) {
            long activeSubscribers = subscriptionRepository.countByPlanIdAndStatus(
                    plan.getId(), SubscriptionStatus.ACTIVE);
            long totalSubscriptions = subscriptionRepository.findByPlanId(plan.getId()).size();

            // Calculate revenue for this plan
            BigDecimal planRevenue = calculateRevenueForPlan(plan.getId());

            Double percentageOfTotal = totalActiveSubscriptions > 0
                    ? (double) activeSubscribers / totalActiveSubscriptions * 100
                    : 0.0;

            popularPlans.add(PlanPopularityResponse.builder()
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .planPrice(plan.getPrice())
                    .activeSubscribers(activeSubscribers)
                    .totalSubscriptions(totalSubscriptions)
                    .totalRevenue(planRevenue)
                    .percentageOfTotal(Math.round(percentageOfTotal * 100.0) / 100.0)
                    .build());
        }

        // Sort by active subscribers descending and assign ranks
        popularPlans.sort((a, b) -> Long.compare(b.getActiveSubscribers(), a.getActiveSubscribers()));
        for (int i = 0; i < popularPlans.size(); i++) {
            popularPlans.get(i).setRank(i + 1);
        }

        return popularPlans;
    }

    /**
     * Get user registration trends.
     * 
     * @return User trends with monthly breakdowns
     */
    public UserTrendsResponse getUserTrends() {
        log.debug("Generating user trends analytics");

        long totalUsers = userRepository.count();

        // This month's users
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

        // Last month's users
        LocalDateTime startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(23, 59, 59);
        long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);

        Double growthPercentage = calculateGrowthPercentage(newUsersThisMonth, newUsersLastMonth);

        // Count active users (users with active subscriptions)
        Set<String> userIdsWithActiveSubscription = subscriptionRepository
                .findByStatus(SubscriptionStatus.ACTIVE)
                .stream()
                .map(s -> s.getUserId())
                .collect(Collectors.toSet());
        long activeUsers = userIdsWithActiveSubscription.size();

        // Conversion rate
        Double conversionRate = totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0.0;

        // Monthly breakdown for last 12 months
        List<MonthlyDataPoint> monthlyRegistrations = getMonthlyUserRegistrations(12);

        return UserTrendsResponse.builder()
                .totalUsers(totalUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersLastMonth(newUsersLastMonth)
                .growthPercentage(growthPercentage)
                .activeUsers(activeUsers)
                .monthlyRegistrations(monthlyRegistrations)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }

    // ========== Helper Methods ==========

    private BigDecimal calculateTotalRevenue() {
        List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);
        return successfulPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueForMonth(YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Payment> payments = paymentRepository.findSuccessfulPaymentsInDateRange(
                PaymentStatus.SUCCESS, startOfMonth, endOfMonth);

        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueForPlan(String planId) {
        // Get all subscriptions for this plan
        var subscriptions = subscriptionRepository.findByPlanId(planId);
        Set<String> subscriptionIds = subscriptions.stream()
                .map(s -> s.getId())
                .collect(Collectors.toSet());

        // Get successful payments for these subscriptions
        List<Payment> allPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);
        return allPayments.stream()
                .filter(p -> subscriptionIds.contains(p.getSubscriptionId()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double calculateGrowthPercentage(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal growth = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return growth.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateGrowthPercentage(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double growth = ((double) current - previous) / previous * 100;
        return Math.round(growth * 100.0) / 100.0;
    }

    private List<MonthlyDataPoint> getMonthlyRevenueBreakdown(int months) {
        List<MonthlyDataPoint> breakdown = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            BigDecimal revenue = calculateRevenueForMonth(month);

            breakdown.add(MonthlyDataPoint.builder()
                    .month(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .year(month.getYear())
                    .value(revenue.doubleValue())
                    .build());
        }

        return breakdown;
    }

    private List<MonthlyDataPoint> getMonthlyUserRegistrations(int months) {
        List<MonthlyDataPoint> breakdown = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

            long count = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

            breakdown.add(MonthlyDataPoint.builder()
                    .month(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .year(month.getYear())
                    .value((double) count)
                    .build());
        }

        return breakdown;
    }
}
