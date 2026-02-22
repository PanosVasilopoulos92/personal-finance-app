package org.viators.personalfinanceapp.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.viators.personalfinanceapp.item.Item;

import java.util.List;

@Schema(description = "Summary view of an item with its price history")
public record ItemSummaryResponse(
        @Schema(description = "Item name", example = "Organic Whole Milk")
        String name,

        @Schema(description = "Item description", example = "Fresh organic whole milk, 1L carton")
        String description

){
    public static ItemSummaryResponse from(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemSummaryResponse(
                item.getName(),
                item.getDescription()
        );
    }

    public static List<ItemSummaryResponse> listOfSummaries(List<Item> items) {
        return items.stream()
                .map(ItemSummaryResponse::from)
                .toList();
    }
}
