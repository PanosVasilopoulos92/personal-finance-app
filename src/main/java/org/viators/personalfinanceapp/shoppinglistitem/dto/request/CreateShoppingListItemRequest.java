package org.viators.personalfinanceapp.shoppinglistitem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.shoppinglist.ShoppingList;
import org.viators.personalfinanceapp.shoppinglistitem.ShoppingListItem;
import org.viators.personalfinanceapp.store.Store;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateShoppingListItemRequest(
        @NotBlank(message = "Item UUID is required")
        String itemUuid,

        @NotBlank(message = "Store UUID is required")
        String storeUuid,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "1.0", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @DecimalMin(value = "0.01", message = "Purchased price must be greater than 0")
        BigDecimal purchasedPrice,

        LocalDate purchasedDate
) {
    public ShoppingListItem toEntity(ShoppingList shoppingList, Item item, Store store) {
        ShoppingListItem shoppingListItem = new ShoppingListItem();
        shoppingListItem.setQuantity(quantity);
        shoppingListItem.setPurchasedPrice(purchasedPrice);
        shoppingListItem.setPurchasedDate(purchasedDate);
        shoppingListItem.setShoppingList(shoppingList);
        shoppingListItem.setItem(item);
        shoppingListItem.setStore(store);
        return shoppingListItem;
    }
}
