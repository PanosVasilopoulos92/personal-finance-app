package org.viators.personalfinanceapp.dto.inflationcalc.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.viators.personalfinanceapp.annotations.validators.ValidDateRange;
import org.viators.personalfinanceapp.model.enums.CurrencyEnum;

import java.time.LocalDate;

@ValidDateRange
public record CreateInflationCalculationRequest(
        @NotNull(message = "Start date of range is required")
        @PastOrPresent
        LocalDate startDate,

        @NotNull(message = "End date of range is required")
        LocalDate endDate,

        @NotNull(message = "Currency is required")
        CurrencyEnum currency,

        @NotNull(message = "Store is required")
        String StoreUuid
) {
}
