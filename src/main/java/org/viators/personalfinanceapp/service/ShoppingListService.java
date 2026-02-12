package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.dto.shoppinglist.request.CreateShoppingListRequest;
import org.viators.personalfinanceapp.dto.shoppinglist.request.UpdateShoppingListRequest;
import org.viators.personalfinanceapp.dto.shoppinglist.response.ShoppingListDetailsResponse;
import org.viators.personalfinanceapp.dto.shoppinglist.response.ShoppingListSummaryResponse;
import org.viators.personalfinanceapp.exceptions.BusinessException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.ShoppingList;
import org.viators.personalfinanceapp.model.ShoppingListItem;
import org.viators.personalfinanceapp.model.User;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.repository.ShoppingListItemRepository;
import org.viators.personalfinanceapp.repository.ShoppingListRepository;
import org.viators.personalfinanceapp.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

    private final UserRepository userRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    @Transactional
    public ShoppingListSummaryResponse create(String userUuid, CreateShoppingListRequest request) {
        User user = userRepository.findByUuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist system"));

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

        ShoppingListItem shoppingListItem = shoppingListItemRepository.findByUuidAndStatus(shoppingListItemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found"));

        shoppingList.addShoppingListItem(shoppingListItem);
        return ShoppingListSummaryResponse.from(shoppingList);
    }

    @Transactional
    public void removeShoppingListItemFromList(String shoppingListUuid, String shoppingListItemUuid) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(shoppingListUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found"));

        ShoppingListItem shoppingListItem = shoppingListItemRepository.findByUuidAndStatus(shoppingListItemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found"));

        if (!shoppingListItemRepository.findAllByShoppingList(shoppingListUuid).contains(shoppingListItem.getId())) {
            throw new BusinessException("Shopping item does not exist in this shopping list");
        }

        shoppingList.removeShoppingListItem(shoppingListItem);
        shoppingListItemRepository.delete(shoppingListItem);
    }

    @Transactional
    public void deactivateShoppingList(String uuid) {
        ShoppingList shoppingList = shoppingListRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found or is already deactivated"));

        shoppingList.setStatus(StatusEnum.INACTIVE.getCode());
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
