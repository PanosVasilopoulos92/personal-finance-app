package org.viators.personalfinanceapp.shoppinglist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.shoppinglist.dto.request.CreateShoppingListRequest;
import org.viators.personalfinanceapp.shoppinglist.dto.request.UpdateShoppingListRequest;
import org.viators.personalfinanceapp.shoppinglist.dto.response.ShoppingListDetailsResponse;
import org.viators.personalfinanceapp.shoppinglist.dto.response.ShoppingListSummaryResponse;
import org.viators.personalfinanceapp.shoppinglistitem.ShoppingListItem;
import org.viators.personalfinanceapp.shoppinglistitem.ShoppingListItemService;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.user.UserService;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    // Other Service dependencies
    private final UserService userService;
    private final ShoppingListItemService shoppingListItemService;


    public ShoppingList getActiveShoppingList(String shoppingListUuid) {
        return shoppingListRepository.findByUuidAndStatus(shoppingListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingList", "uuid", shoppingListUuid));
    }

    @Transactional
    public ShoppingListSummaryResponse create(String userUuid, CreateShoppingListRequest request) {
        User user = userService.findActiveUser(userUuid);

        ShoppingList shoppingList = request.toEntity();
        shoppingList = shoppingListRepository.save(shoppingList);
        return ShoppingListSummaryResponse.from(shoppingList);
    }

    @Transactional
    public ShoppingListSummaryResponse update(String uuid, UpdateShoppingListRequest request) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found or is inactive"));

        request.update(shoppingList);
        return ShoppingListSummaryResponse.from(shoppingList);
    }

    @Transactional
    public ShoppingListSummaryResponse addShoppingListItemToList(String shoppingListUuid, String shoppingListItemUuid) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(shoppingListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found"));

        ShoppingListItem shoppingListItem = shoppingListItemService.getActiveShoppingListItem(shoppingListItemUuid);

        shoppingList.addShoppingListItem(shoppingListItem);
        return ShoppingListSummaryResponse.from(shoppingList);
    }

    @Transactional
    public void removeShoppingListItemFromList(String shoppingListUuid, String shoppingListItemUuid) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(shoppingListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingList", "uuid", shoppingListUuid));

        ShoppingListItem shoppingListItem = shoppingListItemService.getActiveShoppingListItem(shoppingListItemUuid);

        shoppingListItemService.checkSLIExistInShoppingList(shoppingListUuid, shoppingListItem.getId());
        shoppingList.removeShoppingListItem(shoppingListItem);
    }

    @Transactional
    public void deactivateShoppingList(String uuid) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found or is already deactivated"));

        shoppingList.setStatus(StatusEnum.INACTIVE.getCode());
        shoppingList.getShoppingListItems()
                .forEach(sli -> sli.setStatus(StatusEnum.INACTIVE.getCode()));
    }

    public ShoppingListDetailsResponse getShoppingList(String userUuid, String shopListUuid) {
        ShoppingList result = shoppingListRepository.findByUuidAndStatus(shopListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No such shopping list exist in system"));

        return ShoppingListDetailsResponse.from(result, userUuid);
    }

    public Page<ShoppingListSummaryResponse> getAllActiveShoppingListsForUser(String userUuid, Pageable pageable) {
        Page<ShoppingList> results = shoppingListRepository.findAllByUser_UuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode(), pageable);
        return results.map(ShoppingListSummaryResponse::from);
    }

}
