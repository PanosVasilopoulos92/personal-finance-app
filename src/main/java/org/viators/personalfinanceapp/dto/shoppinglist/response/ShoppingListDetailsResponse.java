package org.viators.personalfinanceapp.dto.shoppinglist.response;

import org.viators.personalfinanceapp.dto.shoppinglistitem.response.ShoppingListItemSummaryResponse;
import org.viators.personalfinanceapp.model.ShoppingList;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingListDetailsResponse(
        String uuid,
        String name,
        String description,
        BigDecimal totalAmount,
        Boolean isFavorite,
        String userUuid,
        List<ShoppingListItemSummaryResponse> items
) {

    public static ShoppingListDetailsResponse from(ShoppingList shoppingList, String userUuid) {
        return new ShoppingListDetailsResponse(
                shoppingList.getUuid(),
                shoppingList.getName(),
                shoppingList.getDescription(),
                shoppingList.getTotalAmount(),
                shoppingList.getIsFavorite(),
                userUuid,
                ShoppingListItemSummaryResponse.listOfSummaries(shoppingList.getShoppingListItems())
        );
    }
}
