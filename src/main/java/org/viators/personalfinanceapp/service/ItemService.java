package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.dto.item.request.CreateItemRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemPriceRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemRequest;
import org.viators.personalfinanceapp.dto.item.response.ItemDetailsResponse;
import org.viators.personalfinanceapp.dto.item.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.exceptions.BusinessValidationException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.*;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.repository.*;
import org.viators.personalfinanceapp.security.OwnershipAuthorizationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final PriceObservationRepository priceObservationRepository;
    private final OwnershipAuthorizationService ownershipAuthorizationService;

    public ItemDetailsResponse getItem(String uuid, String loggedInUserUuid) {
        Item item = itemRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item does not exist"));

        ownershipAuthorizationService.verifyOwnership(loggedInUserUuid, item.getUser().getUuid());
        return ItemDetailsResponse.from(item);
    }

    public Page<ItemSummaryResponse> getItems(String loggedInUserUuid, Pageable pageable) {
        User user = userRepository.findByUuidAndStatus(loggedInUserUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User", "uuid", loggedInUserUuid));

        return itemRepository.findAllByUser_UuidAndStatus(loggedInUserUuid, StatusEnum.ACTIVE.getCode(), pageable)
                .map(ItemSummaryResponse::from);
    }

    @Transactional
    public ItemSummaryResponse create(String loggedInUserUuid, CreateItemRequest request) {
        User user = userRepository.findByUuidAndStatus(loggedInUserUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system"));

        Store store = storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(request.createPriceObservationRequest().storeUuid(),
                        StatusEnum.ACTIVE.getCode(), loggedInUserUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Store could not be found"));

        Item item = request.toEntity();
        item.setUser(user);

        PriceObservation priceObservation = request.createPriceObservationRequest().toEntity();
        priceObservation.setStore(store);
        item.addPriceObservation(priceObservation);

        item = itemRepository.save(item);
        return ItemSummaryResponse.from(item);
    }

    @Transactional
    public ItemSummaryResponse update(String loggedInUserUuid, UpdateItemRequest request) {
        User user = userRepository.findByUuidAndStatus(loggedInUserUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system"));

        Item item = itemRepository.findByUuidAndStatus(request.itemUuid(), StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getUser() != user) {
            throw new AccessDeniedException("User can update items that belong to him/her only");
        }

        if (request.categoryUuid() != null) {
            Category category = categoryRepository.findByUuid(request.categoryUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            if (itemRepository.existsByNameAndUser_IdAndStatusAndCategoriesContaining(request.newName(),
                    user.getId(), StatusEnum.ACTIVE.getCode(), category)) {
                throw new BusinessValidationException("Categories cannot contain items with same name") {
                };
            }
        } else {
            if (itemRepository.existsByNameAndUser_IdAndStatusAndCategoriesIsEmpty(request.newName(),
                    user.getId(), StatusEnum.ACTIVE.getCode())) {
                throw new BusinessValidationException("User's items cannot have same name unless they belong to different categories");
            }
        }

        request.updateItem(item);

        return ItemSummaryResponse.from(item);
    }

    @Transactional
    public ItemSummaryResponse updatePrice(String userUuid, String itemUuid, UpdateItemPriceRequest request) {
        User user = userRepository.findByUuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system"));


        Item item = itemRepository.findByUuidAndStatus(itemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemUuid));

        Store store = storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(
                        request.createPriceObservationRequest().storeUuid(),
                        StatusEnum.ACTIVE.getCode(),
                        userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "uuid", request.createPriceObservationRequest().storeUuid()));

        ownershipAuthorizationService.verifyOwnership(userUuid, item.getUser().getUuid());

        PriceObservation lastPriceObservation = priceObservationRepository.findLastActivePriceObservation(item.getUuid(), StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No active price found for this item"));
        lastPriceObservation.setStatus(StatusEnum.INACTIVE.getCode());

        PriceObservation newPriceObservation = request.createPriceObservationRequest().toEntity();
        newPriceObservation.setStore(store);
        item.addPriceObservation(newPriceObservation);

        return ItemSummaryResponse.from(item);
    }

    @Transactional
    public void deactivateItem(String userUuid, String itemUuid) {
        Item item = itemRepository.findByUuidAndStatus(itemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemUuid));

        ownershipAuthorizationService.verifyOwnership(userUuid, item.getUser().getUuid());
        item.setStatus(StatusEnum.INACTIVE.getCode());
    }
}
