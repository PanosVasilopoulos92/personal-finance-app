package org.viators.personalfinanceapp.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.viators.personalfinanceapp.priceobservation.dto.request.CreatePriceObservationRequest;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.common.enums.ItemUnitEnum;

import java.util.Optional;

@Schema(description = "Payload for creating a new item with its initial price observation")
public record CreateItemRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        @Schema(description = "Item name", example = "Milk 1,5%")
        String name,

        @Size(min = 5, max = 300, message = "Description must be between 5 and 300 characters")
        @Schema(description = "Item description", example = "Fresh whole milk, 1L carton", nullable = true)
        String description,

        @Schema(description = "Unit of measurement for the item", example = "LITER")
        @NotNull(message = "Unit is required")
        ItemUnitEnum itemUnit, // Throws 'InvalidFormatException' if user provide a not available option

        @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
        @Schema(description = "Item's brand", example = "Sklavenitis", nullable = true)
        String brand,

        @Valid
        @Schema(description = "Initial price observation for this item")
        CreatePriceObservationRequest createPriceObservationRequest
) {

    public Item toEntity() {
        Item entity = new Item();
        Optional.ofNullable(name).ifPresent(entity::setName);
        Optional.ofNullable(description).ifPresent(entity::setDescription);
        Optional.ofNullable(itemUnit).ifPresent(entity::setItemUnit);
        Optional.ofNullable(brand).ifPresent(entity::setBrand);

        return entity;
    }
}
