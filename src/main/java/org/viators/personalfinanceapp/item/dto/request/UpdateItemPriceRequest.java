package org.viators.personalfinanceapp.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.viators.personalfinanceapp.priceobservation.dto.request.CreatePriceObservationRequest;

@Schema(description = "Payload for updating an item's price by creating a new price observation")
public record UpdateItemPriceRequest(
        @Valid
        @Schema(description = "The new price observation to record")
        CreatePriceObservationRequest createPriceObservationRequest
) {
}
