package com.example.subscription_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Helper DTO for monthly data points in analytics.
 * Used for monthly breakdowns in revenue and user trends.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDataPoint {

    /**
     * Month name (e.g., "January", "February").
     */
    private String month;

    /**
     * Year of the data point.
     */
    private Integer year;

    /**
     * Numeric value (revenue amount or count).
     */
    private Double value;

    /**
     * Factory method for creating a data point.
     */
    public static MonthlyDataPoint of(String month, Integer year, Double value) {
        return MonthlyDataPoint.builder()
                .month(month)
                .year(year)
                .value(value)
                .build();
    }
}
