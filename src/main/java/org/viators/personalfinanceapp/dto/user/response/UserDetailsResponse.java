package org.viators.personalfinanceapp.dto.user.response;

import org.viators.personalfinanceapp.dto.basket.response.BasketSummary;
import org.viators.personalfinanceapp.dto.category.response.CategorySummary;
import org.viators.personalfinanceapp.dto.inflationreport.response.InflationReportSummary;
import org.viators.personalfinanceapp.dto.item.response.ItemSummary;
import org.viators.personalfinanceapp.dto.pricealert.response.PriceAlertSummary;
import org.viators.personalfinanceapp.dto.shoppinglist.response.ShoppingListSummary;
import org.viators.personalfinanceapp.dto.userpreferences.response.UserPreferencesSummary;
import org.viators.personalfinanceapp.model.User;
import org.viators.personalfinanceapp.model.enums.UserRolesEnum;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetailsResponse(
        String uuid,
        String username,
        String fullName,
        String email,
        Boolean isActive,
        UserRolesEnum userRole,
        LocalDateTime createdAt,
        UserPreferencesSummary userPreferences,
        List<ItemSummary> items,
        List<CategorySummary> categories,
        List<PriceAlertSummary> priceAlerts,
        List<ShoppingListSummary> shoppingLists,
        List<InflationReportSummary> inflationReports,
        List<BasketSummary> baskets
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
                UserPreferencesSummary.from(user.getUserPreferences()),
                ItemSummary.listOfSummaries(user.getItems()),
                CategorySummary.listOfSummaries(user.getCategories()),
                PriceAlertSummary.listOfSummaries(user.getPriceAlerts()),
                ShoppingListSummary.listOfSummaries(user.getShoppingLists()),
                InflationReportSummary.listOfSummaries(user.getInflationReports()),
                BasketSummary.listOfSummaries(user.getBaskets())
        );
    }
}