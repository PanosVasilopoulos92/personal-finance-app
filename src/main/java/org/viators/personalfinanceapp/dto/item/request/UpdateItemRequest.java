package org.viators.personalfinanceapp.dto.item.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.viators.personalfinanceapp.model.Item;
import org.viators.personalfinanceapp.model.enums.ItemUnitEnum;

import java.util.Optional;

public record UpdateItemRequest(
        @NotBlank(message = "User's uuid is required for update operation")
        @Size(min = 10, max = 50, message = "Uuid must be 10-50 chars")
        @Schema(description = "UUID of the user performing the update",
                example = "b4c2d1e0-3f5a-6b7c-8d9e-0f1a2b3c4d5e")
        String userUuid,

        @Schema(description = "UUID of the item to update",
                example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
        String itemUuid,

        @Schema(description = "UUID of the category to assign (null to keep current)",
                example = "d6e4f3a2-5b7c-8d9e-0f1a-2b3c4d5e6f7a",
                nullable = true)
        String categoryUuid,

        @Schema(description = "New name for the item", example = "Semi-Skimmed Milk")
        String newName,

        @Schema(description = "Updated description",
                example = "Semi-skimmed milk, 1.5L carton",
                nullable = true)
        String description,

        @Schema(description = "Updated unit of measurement", example = "LITER")
        ItemUnitEnum itemUnit,

        @Schema(description = "Updated brand name", example = "Delta", nullable = true)
        String brand,

        @Schema(description = "Mark or unmark item as favorite", example = "true")
        Boolean isFavorite
) {

    public void updateItem(Item item) {
        Optional.ofNullable(newName).ifPresent(item::setName);
        Optional.ofNullable(description).ifPresent(item::setDescription);
        Optional.ofNullable(itemUnit).ifPresent(item::setItemUnit);
        Optional.ofNullable(brand).ifPresent(item::setBrand);
        Optional.ofNullable(isFavorite).ifPresent(item::setIsFavorite);

    }
}
