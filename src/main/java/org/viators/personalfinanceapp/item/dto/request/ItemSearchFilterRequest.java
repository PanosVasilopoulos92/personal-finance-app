package org.viators.personalfinanceapp.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.viators.personalfinanceapp.common.enums.ItemUnitEnum;
import org.viators.personalfinanceapp.common.enums.StatusEnum;

import java.time.Instant;

public record ItemSearchFilterRequest(

        @Schema(description = "Keyword to search item name (case-insensitive, partial match)",
                example = "milk",
                nullable = true)
        String nameKeyword,

        @Schema(description = "Filter by item status. Defaults to ACTIVE when omitted.",
                example = "ACTIVE",
                nullable = true)
        StatusEnum status,

        @Schema(description = "Filter by brand name",
                example = "Delta",
                nullable = true)
        String brand,

        @Schema(description = "Filter by unit of measurement",
                example = "LITER",
                nullable = true)
        ItemUnitEnum itemUnit,

        @Schema(description = "When true, returns only favorite items",
                example = "true",
                nullable = true)
        Boolean isFavorite,

        @Schema(description = "Filter by category UUID",
                example = "d6e4f3a2-5b7c-8d9e-0f1a-2b3c4d5e6f7a",
                nullable = true)
        String categoryUuid,

        @Schema(description = "Return items created at or after this timestamp",
                example = "2024-01-01T00:00:00Z",
                nullable = true)
        Instant createdAfter,

        @Schema(description = "Return items created at or before this timestamp",
                example = "2024-12-31T23:59:59Z",
                nullable = true)
        Instant createdBefore
) {
}
