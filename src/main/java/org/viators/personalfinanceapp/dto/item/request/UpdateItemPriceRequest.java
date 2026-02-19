package org.viators.personalfinanceapp.dto.item.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.viators.personalfinanceapp.dto.priceobservation.request.CreatePriceObservationRequest;

@Schema(description = "Payload for updating an item's price by creating a new price observation")
public record UpdateItemPriceRequest(
        @Valid
        @Schema(description = "The new price observation to record")
        CreatePriceObservationRequest createPriceObservationRequest
) {
}
