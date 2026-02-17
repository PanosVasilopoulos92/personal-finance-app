package org.viators.personalfinanceapp.dto.item.request;

import jakarta.validation.Valid;
import org.viators.personalfinanceapp.dto.priceobservation.request.CreatePriceObservationRequest;

public record UpdateItemPriceRequest(
        @Valid
        CreatePriceObservationRequest createPriceObservationRequest
) {
}
