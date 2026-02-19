package org.viators.personalfinanceapp.dto.priceobservation.response;

import org.viators.personalfinanceapp.dto.item.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.dto.store.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.model.PriceObservation;
import org.viators.personalfinanceapp.model.enums.CurrencyEnum;
import org.viators.personalfinanceapp.model.enums.StatusEnum;

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
