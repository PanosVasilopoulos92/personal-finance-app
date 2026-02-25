package org.viators.personalfinanceapp.shoppinglistitem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.item.ItemService;
import org.viators.personalfinanceapp.shoppinglist.ShoppingList;
import org.viators.personalfinanceapp.shoppinglist.ShoppingListService;
import org.viators.personalfinanceapp.shoppinglistitem.dto.request.CreateShoppingListItemRequest;
import org.viators.personalfinanceapp.shoppinglistitem.dto.response.ShoppingListItemSummaryResponse;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.store.StoreService;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.user.UserService;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListItemService {

    private final ShoppingListItemRepository shoppingListItemRepository;

    // Other Service Dependencies
    private final UserService userService;
    private final ShoppingListService shoppingListService;
    private final ItemService itemService;
    private final StoreService storeService;

    @Transactional
    public ShoppingListItemSummaryResponse create(String userUuid, String shoppingListUuid, CreateShoppingListItemRequest request) {
        // Handle relationships
        User user = userService.findActiveUser(userUuid);
        ShoppingList shoppingList = shoppingListService.getActiveShoppingList(shoppingListUuid);
        Item item = itemService.getActiveItem(request.itemUuid());
        Store store = storeService.getActiveStoreThatIsGlobalOrBelongsToUser(request.storeUuid(), user.getUuid());

        ShoppingListItem shoppingListItem = request.toEntity(shoppingList, item, store);
        shoppingListItem = shoppingListItemRepository.save(shoppingListItem);

        return ShoppingListItemSummaryResponse.from(shoppingListItem);
    }

    public ShoppingListItem getActiveShoppingListItem(String shoppingListItemUuid) {
        return shoppingListItemRepository.findByUuidAndStatus(shoppingListItemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item", "uuid", shoppingListItemUuid));
    }

    public void checkSLIExistInShoppingList(String shoppingListUuid, Long sliId) {
        if (!shoppingListItemRepository.findAllByShoppingList(shoppingListUuid).contains(sliId)) {
            throw new IllegalArgumentException("Shopping item does not exist in this shopping list");
        }
    }

}
