package org.viators.personalfinanceapp.dto.shoppinglist.response;

import org.viators.personalfinanceapp.model.ShoppingList;
import java.util.List;

public record ShoppingListSummaryResponse(
        String uuid,
        String name,
        String description,
        Boolean isFavorite,
        int numberOfItems
) {

    public static ShoppingListSummaryResponse from(ShoppingList shoppingList) {
        return new ShoppingListSummaryResponse(
                shoppingList.getUuid(),
                shoppingList.getName(),
                shoppingList.getDescription(),
                shoppingList.getIsFavorite(),
                shoppingList.getShoppingListItems().size()
        );
    }

    public static List<ShoppingListSummaryResponse> listOfSummaries(List<ShoppingList> shoppingLists) {
        return shoppingLists.stream()
                .map(ShoppingListSummaryResponse::from)
                .toList();
    }
}
