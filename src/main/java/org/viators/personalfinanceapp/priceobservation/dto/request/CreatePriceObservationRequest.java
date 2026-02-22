package org.viators.personalfinanceapp.priceobservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.viators.personalfinanceapp.priceobservation.PriceObservation;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Schema(description = "Price observation data — records a price seen at a specific store and time")
public record CreatePriceObservationRequest(
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
        @Schema(description = "Observed price", example = "2.49")
        BigDecimal price,

        @NotNull(message = "Currency is required")
        @Schema(description = "Currency of the observed price", example = "EUR")
        CurrencyEnum currency,

        @NotNull(message = "Date of observation is required")
        @PastOrPresent(message = "Observation date cannot be in the future")
        @Schema(description = "When the price was observed",
                example = "2026-02-18T10:30:00Z")
        LocalDate observationDate,

        @NotBlank(message = "Location is required")
        @Size(max = 100, message = "Location must not exceed 100 characters")
        @Schema(description = "Store location or address where price was observed",
                example = "Chaidari, Plato 469")
        String location,

        @Size(max = 400, message = "Notes must not exceed 400 characters")
        @Schema(description = "Optional notes about the observation",
                example = "Weekend promotion price",
                nullable = true)
        String notes,

        @NotBlank(message = "Store is required")
        @Schema(description = "UUID of the store where the price was observed",
                example = "a3f1b2c4-5d6e-7f8a-9b0c-1d2e3f4a5b6c")
        String storeUuid
) {
    public PriceObservation toEntity() {
        PriceObservation priceObservation = new PriceObservation();
        Optional.ofNullable(price).ifPresent(priceObservation::setPrice);
        Optional.ofNullable(currency).ifPresent(priceObservation::setCurrency);
        Optional.ofNullable(observationDate).ifPresent(priceObservation::setObservationDate);
        Optional.ofNullable(location).ifPresent(priceObservation::setLocation);
        Optional.ofNullable(notes).ifPresent(priceObservation::setNotes);

        return priceObservation;
    }
}
