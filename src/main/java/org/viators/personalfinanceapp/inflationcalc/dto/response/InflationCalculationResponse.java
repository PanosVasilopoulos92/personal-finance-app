package org.viators.personalfinanceapp.inflationcalc.dto.response;

import org.viators.personalfinanceapp.common.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InflationCalculationResponse(
        BigDecimal startedPrice,
        BigDecimal lastPrice,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal inflationRate,
        BigDecimal priceDifference,
        CurrencyEnum currency,
        String insufficientDataMessage
) {

    public static InflationCalculationResponse insufficientData() {
        return new InflationCalculationResponse(
                null, null, null, null, null, null, null,
                "Not enough price observations in the given date range to calculate inflation. At least 2 are required."
        );
    }
}
