package org.viators.personalfinanceapp.dto.priceobservation.response;

import org.viators.personalfinanceapp.dto.store.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.model.PriceObservation;
import org.viators.personalfinanceapp.model.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PriceObservationSummaryResponse(
        BigDecimal price,
        CurrencyEnum currency,
        Instant observationDate,
        StoreSummaryResponse storeSummaryResponse
) {
    public static PriceObservationSummaryResponse from(PriceObservation priceObservation) {
        return new PriceObservationSummaryResponse(
                priceObservation.getPrice(),
                priceObservation.getCurrency(),
                priceObservation.getObservationDate(),
                StoreSummaryResponse.from(priceObservation.getStore())
        );
    }

    public static List<PriceObservationSummaryResponse> listOfSummaries(List<PriceObservation> priceObservations) {
        return priceObservations.stream()
                .map(PriceObservationSummaryResponse::from)
                .toList();
    }
}
