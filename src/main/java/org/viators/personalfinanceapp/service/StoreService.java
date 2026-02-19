package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.dto.store.request.CreateStoreRequest;
import org.viators.personalfinanceapp.dto.store.request.UpdateStoreRequest;
import org.viators.personalfinanceapp.dto.store.response.StoreDetailsResponse;
import org.viators.personalfinanceapp.dto.store.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.User;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.repository.StoreRepository;
import org.viators.personalfinanceapp.repository.UserRepository;
import org.viators.personalfinanceapp.security.OwnershipAuthorizationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OwnershipAuthorizationService ownershipAuthorizationService;

    public Page<StoreSummaryResponse> getStores(String userUuid, Pageable pageable) {
        Page<Store> stores = storeRepository.findAllAvailableStoresForUser(StatusEnum.ACTIVE.getCode(), userUuid, pageable);
        return stores.map(StoreSummaryResponse::from);
    }

    public StoreDetailsResponse getStore(String userUuid, String storeUuid) {
        Store store = storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(
                        storeUuid, StatusEnum.ACTIVE.getCode(), userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeUuid));

        return StoreDetailsResponse.from(store);
    }

    @Transactional
    public StoreSummaryResponse create(String userUuid, CreateStoreRequest request) {
        User user = userRepository.findByUuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        if (storeRepository.existsByNameIgnoreCaseAndStatusAndUserIsNotNullAndUser_Uuid(request.name(),
                StatusEnum.ACTIVE.getCode(), userUuid)) {
            throw new DuplicateResourceException("Store", "name", request.name());
        }

        Store store = request.toEntity();
        user.addStore(store);

        store = storeRepository.save(store);
        return StoreSummaryResponse.from(store);
    }

    @Transactional
    public StoreSummaryResponse update(String userUuid, String storeUuid, UpdateStoreRequest request) {

        Store store = storeRepository.findByUuidAndStatus(storeUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeUuid));

        ownershipAuthorizationService.verifyOwnership(userUuid, store.getUser().getUuid());

        request.updateStore(store);
        return StoreSummaryResponse.from(store);
    }

    @Transactional
    public void deActivateStore(String userUuid, String storeUuid) {
        Store store = storeRepository.findByUuidAndStatusAndUserIsNotNull(storeUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Store with uuid: %s not found or is already inactive.".formatted(storeUuid)));

        ownershipAuthorizationService.verifyOwnership(userUuid, store.getUser().getUuid());
        store.setStatus(StatusEnum.INACTIVE.getCode());
    }

    @Transactional
    public void reActivateStore(String userUuid, String storeUuid) {
        Store store = storeRepository.findByUuidAndStatusAndUserIsNotNull(storeUuid, StatusEnum.INACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Store with uuid: %s not found or is already active.".formatted(storeUuid)));

        ownershipAuthorizationService.verifyOwnership(userUuid, store.getUser().getUuid());
        store.setStatus(StatusEnum.ACTIVE.getCode());
    }

    @Transactional
    public void deleteStore(String storeUuid) {
        Store store = storeRepository.findByUuidAndStatusAndUserIsNull(storeUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store with uuid: %s not found or is inactive or belongs to a user".formatted(storeUuid))
                );

        storeRepository.delete(store);
    }

}
