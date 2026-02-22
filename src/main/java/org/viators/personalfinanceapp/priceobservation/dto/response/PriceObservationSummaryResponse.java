package org.viators.personalfinanceapp.priceobservation.dto.response;

import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.store.dto.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.priceobservation.PriceObservation;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;
import org.viators.personalfinanceapp.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PriceObservationSummaryResponse(
        BigDecimal price,
        CurrencyEnum currency,
        LocalDate observationDate,
        String location,
        StatusEnum status,
        StoreSummaryResponse storeSummary,
        ItemSummaryResponse itemSummary
) {
    public static PriceObservationSummaryResponse from(PriceObservation priceObservation) {
        return new PriceObservationSummaryResponse(
                priceObservation.getPrice(),
                priceObservation.getCurrency(),
                priceObservation.getObservationDate(),
                priceObservation.getLocation(),
                StatusEnum.getStatusFromCode(priceObservation.getStatus()),
                StoreSummaryResponse.from(priceObservation.getStore()),
                ItemSummaryResponse.from(priceObservation.getItem())
        );
    }

    public static List<PriceObservationSummaryResponse> listOfSummaries(List<PriceObservation> priceObservations) {
        return priceObservations.stream()
                .map(PriceObservationSummaryResponse::from)
                .toList();
    }
}
