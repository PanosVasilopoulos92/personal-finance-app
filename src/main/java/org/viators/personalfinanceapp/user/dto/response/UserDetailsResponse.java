package org.viators.personalfinanceapp.user.dto.response;

import org.viators.personalfinanceapp.basket.dto.response.BasketSummaryResponse;
import org.viators.personalfinanceapp.category.dto.response.CategorySummaryResponse;
import org.viators.personalfinanceapp.inflationreport.dto.response.InflationReportSummaryResponse;
import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.pricealert.dto.response.PriceAlertSummaryResponse;
import org.viators.personalfinanceapp.shoppinglist.dto.response.ShoppingListSummaryResponse;
import org.viators.personalfinanceapp.userpreferences.dto.response.UserPreferencesSummaryResponse;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.common.enums.UserRolesEnum;

import java.time.Instant;
import java.util.List;

public record UserDetailsResponse(
        String uuid,
        String username,
        String fullName,
        String email,
        Boolean isActive,
        UserRolesEnum userRole,
        Instant createdAt,
        UserPreferencesSummaryResponse userPreferences,
        List<ItemSummaryResponse> items,
        List<CategorySummaryResponse> categories,
        List<PriceAlertSummaryResponse> priceAlerts,
        List<ShoppingListSummaryResponse> shoppingLists,
        List<InflationReportSummaryResponse> inflationReports,
        List<BasketSummaryResponse> baskets
) {

    public static UserDetailsResponse from(User user) {
        return new UserDetailsResponse(
                user.getUsername(),
                user.getUuid(),
                user.getFirstName().concat(" ").concat(user.getLastName()),
                user.getEmail(),
                user.getStatus().equals("1"),
                user.getUserRole(),
                user.getCreatedAt(),
                UserPreferencesSummaryResponse.from(user.getUserPreferences()),
                ItemSummaryResponse.listOfSummaries(user.getItems()),
                CategorySummaryResponse.listOfSummaries(user.getCategories()),
                PriceAlertSummaryResponse.listOfSummaries(user.getPriceAlerts()),
                ShoppingListSummaryResponse.listOfSummaries(user.getShoppingLists()),
                InflationReportSummaryResponse.listOfSummaries(user.getInflationReports()),
                BasketSummaryResponse.listOfSummaries(user.getBaskets())
        );
    }
}