package org.viators.personalfinanceapp.dto.pricecomparison.response;

import org.viators.personalfinanceapp.model.PriceComparison;

import java.time.LocalDate;

public record PriceComparisonSummary(
        LocalDate comparisonDate,
        String itemName,
        String bestStoreName
) {
    public static PriceComparisonSummary from(PriceComparison entity) {
        return new PriceComparisonSummary(
                entity.getComparisonDate(),
                entity.getItem().getName(),
                entity.getBestStore().getName()
        );
    }
}