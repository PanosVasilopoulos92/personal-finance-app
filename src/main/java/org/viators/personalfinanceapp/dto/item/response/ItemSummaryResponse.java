package org.viators.personalfinanceapp.dto.item.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.viators.personalfinanceapp.dto.priceobservation.response.PriceObservationSummaryResponse;
import org.viators.personalfinanceapp.model.Item;

import java.util.List;

@Schema(description = "Summary view of an item with its price history")
public record ItemSummaryResponse(
        @Schema(description = "Item name", example = "Organic Whole Milk")
        String name,

        @Schema(description = "Item description", example = "Fresh organic whole milk, 1L carton")
        String description,

        @Schema(description = "Price observations recorded for this item")
        List<PriceObservationSummaryResponse> priceObservationSummaryResponse
){
    public static ItemSummaryResponse from(Item item) {
        return new ItemSummaryResponse(
                item.getName(),
                item.getDescription(),
                PriceObservationSummaryResponse.listOfSummaries(item.getPriceObservations())
        );
    }

    public static List<ItemSummaryResponse> listOfSummaries(List<Item> items) {
        return items.stream()
                .map(ItemSummaryResponse::from)
                .toList();
    }
}
