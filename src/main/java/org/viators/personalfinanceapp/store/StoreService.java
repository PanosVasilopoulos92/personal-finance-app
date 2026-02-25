package org.viators.personalfinanceapp.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.security.OwnershipAuthorizationService;
import org.viators.personalfinanceapp.store.dto.request.CreateStoreRequest;
import org.viators.personalfinanceapp.store.dto.request.StoreFilterRequest;
import org.viators.personalfinanceapp.store.dto.request.UpdateStoreRequest;
import org.viators.personalfinanceapp.store.dto.response.StoreDetailsResponse;
import org.viators.personalfinanceapp.store.dto.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    // Other Service Dependencies
    private final UserService userService;
    private final OwnershipAuthorizationService ownershipAuthorizationService;

    public Store getActiveStoreThatIsGlobalOrBelongsToUser(String storeUuid, String loggedInUserUuid) {
        return storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(storeUuid,
                        StatusEnum.ACTIVE.getCode(),
                        loggedInUserUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Store was not found for this user"));
    }

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
        User user = userService.findActiveUser(userUuid);

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

    public Page<StoreSummaryResponse> getStoresBasedOnFilters(String userUuid, StoreFilterRequest filter, Pageable pageable) {
        Specification<Store> specs = Specification.where(StoreSpecs.belongsToUser(userUuid));

        if (filter.storeType() != null) {
            specs = specs.and(StoreSpecs.hasStoreType(filter.storeType()));
        }

        if (filter.inCity() != null) {
            specs = specs.and(StoreSpecs.inCity(filter.inCity()));
        }

        if (filter.inCountry() != null) {
            specs = specs.and(StoreSpecs.inCountry(filter.inCountry()));
        }

        if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
            specs = specs.and(StoreSpecs.nameContaining(filter.nameContains()));
        }

        if (filter.hasWebsite() != null) {
            specs = specs.and(StoreSpecs.hasWebsite());
        }

        if (filter.status() != null) {
            specs = specs.and(StoreSpecs.hasStatus(filter.status()));
        }

        if (filter.locationContains() != null && !filter.locationContains().isBlank()) {
            specs = specs.and(StoreSpecs.locationContains(filter.locationContains()));
        }

        return storeRepository.findAll(specs, pageable)
                .map(StoreSummaryResponse::from);
    }
}
