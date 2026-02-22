package org.viators.personalfinanceapp.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.viators.personalfinanceapp.category.dto.response.CategorySummaryResponse;
import org.viators.personalfinanceapp.pricealert.dto.response.PriceAlertSummaryResponse;
import org.viators.personalfinanceapp.pricecomparison.dto.response.PriceComparisonSummaryResponse;
import org.viators.personalfinanceapp.priceobservation.dto.response.PriceObservationSummaryResponse;
import org.viators.personalfinanceapp.user.dto.response.UserSummaryResponse;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.common.enums.ItemUnitEnum;

import java.time.Instant;
import java.util.List;

@Schema(description = "Detailed view of an item including categories, price history, alerts, and comparisons")
public record ItemDetailsResponse(
        @Schema(description = "Item UUID", example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
        String uuid,

        @Schema(description = "Item name", example = "Organic Whole Milk")
        String name,

        @Schema(description = "Item description", example = "Fresh organic whole milk, 1L carton")
        String description,

        @Schema(description = "Unit of measurement", example = "LITTER")
        ItemUnitEnum itemUnit,

        @Schema(description = "Brand name", example = "Noy-Noy")
        String brand,

        @Schema(description = "Whether the user marked this as a favorite", example = "false")
        boolean isFavorite,

        @Schema(description = "Record status: 1=Active, 0=Inactive", example = "1")
        String status,

        @Schema(description = "When the item was created")
        Instant createdAt,

        @Schema(description = "When the item was last updated")
        Instant updatedAt,

        @Schema(description = "Owner of this item")
        UserSummaryResponse user,

        @Schema(description = "Categories this item belongs to")
        List<CategorySummaryResponse> categories,

        @Schema(description = "All recorded price observations")
        List<PriceObservationSummaryResponse> priceObservations,

        @Schema(description = "Active price alerts for this item")
        List<PriceAlertSummaryResponse> priceAlerts,

        @Schema(description = "Price comparisons across stores")
        List<PriceComparisonSummaryResponse> priceComparisons
) {
    public static ItemDetailsResponse from(Item item) {
        return new ItemDetailsResponse(
                item.getUuid(),
                item.getName(),
                item.getDescription(),
                item.getItemUnit(),
                item.getBrand(),
                item.getIsFavorite(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                UserSummaryResponse.from(item.getUser()),
                CategorySummaryResponse.listOfSummaries(item.getCategories()),
                PriceObservationSummaryResponse.listOfSummaries(item.getPriceObservations()),
                PriceAlertSummaryResponse.listOfSummaries(item.getPriceAlerts()),
                PriceComparisonSummaryResponse.listOfSummaries(item.getPriceComparisons())
        );
    }
}
