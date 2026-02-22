package org.viators.personalfinanceapp.category.dto.response;

import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.user.dto.response.UserSummaryResponse;
import org.viators.personalfinanceapp.category.Category;

import java.util.List;

public record CategoryDetailsResponse(
        String name,
        String description,
        UserSummaryResponse user,
        List<ItemSummaryResponse> items
) {
    public static CategoryDetailsResponse from(Category category) {
        return new CategoryDetailsResponse(
                category.getName(),
                category.getDescription(),
                UserSummaryResponse.from(category.getUser()),
                ItemSummaryResponse.listOfSummaries(category.getItems())
        );
    }
}
