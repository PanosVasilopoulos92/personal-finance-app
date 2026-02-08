package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.dto.shoppinglistitem.request.CreateShoppingListItemRequest;
import org.viators.personalfinanceapp.dto.shoppinglistitem.response.ShoppingListItemSummaryResponse;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.*;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.repository.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListItemService {

    private final UserRepository userRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    @Transactional
    public ShoppingListItemSummaryResponse create(String userUuid, String shoppingListUuid, CreateShoppingListItemRequest request) {
        User user = userRepository.findByUuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User not found in system"));

        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(shoppingListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found"));

        Item item = itemRepository.findByUuidAndStatus(request.itemUuid(), StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item wasn't found"));

        Store store = storeRepository.findByUuidAndStatus(request.storeUuid(), StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        ShoppingListItem shoppingListItem = request.toEntity(shoppingList, item, store);
        shoppingListItem = shoppingListItemRepository.save(shoppingListItem);

        return ShoppingListItemSummaryResponse.from(shoppingListItem);
    }
}
