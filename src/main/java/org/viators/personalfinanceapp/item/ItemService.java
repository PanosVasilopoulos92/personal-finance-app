package org.viators.personalfinanceapp.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.category.Category;
import org.viators.personalfinanceapp.category.CategoryService;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.BusinessValidationException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.inflationcalc.dto.response.InflationCalculationResponse;
import org.viators.personalfinanceapp.item.dto.request.CreateItemRequest;
import org.viators.personalfinanceapp.item.dto.request.ItemSearchFilterRequest;
import org.viators.personalfinanceapp.item.dto.request.UpdateItemPriceRequest;
import org.viators.personalfinanceapp.item.dto.request.UpdateItemRequest;
import org.viators.personalfinanceapp.item.dto.response.ItemDetailsResponse;
import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.priceobservation.PriceObservation;
import org.viators.personalfinanceapp.priceobservation.PriceObservationService;
import org.viators.personalfinanceapp.priceobservation.PriceObservationSpecs;
import org.viators.personalfinanceapp.priceobservation.dto.response.PriceObservationSummaryResponse;
import org.viators.personalfinanceapp.security.OwnershipAuthorizationService;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.store.StoreService;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.user.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    // Other Service dependencies
    private final UserService userService;
    private final CategoryService categoryService;
    private final StoreService storeService;
    private final PriceObservationService priceObservationService;
    private final OwnershipAuthorizationService ownershipAuthorizationService;


    public Item getActiveItem(String itemUuid) {
        return itemRepository.findByUuidAndStatus(itemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "uuid", itemUuid));
    }

    public ItemDetailsResponse getItem(String uuid, String loggedInUserUuid) {
        Item item = itemRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item does not exist"));

        ownershipAuthorizationService.verifyOwnership(loggedInUserUuid, item.getUser().getUuid());
        return ItemDetailsResponse.from(item);
    }

    public Page<ItemSummaryResponse> getItems(String loggedInUserUuid, Pageable pageable) {
        User user = userService.findActiveUser(loggedInUserUuid);

        return itemRepository.findAllByUser_UuidAndStatus(loggedInUserUuid, StatusEnum.ACTIVE.getCode(), pageable)
                .map(ItemSummaryResponse::from);
    }

    @Transactional
    public ItemSummaryResponse create(String loggedInUserUuid, CreateItemRequest request) {
        User user = userService.findActiveUser(loggedInUserUuid);

        Store store = storeService.getActiveStoreThatIsGlobalOrBelongsToUser(
                request.createPriceObservationRequest().storeUuid(), loggedInUserUuid);

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
        User user = userService.findActiveUser(loggedInUserUuid);

        Item item = itemRepository.findByUuidAndStatus(request.itemUuid(), StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getUser() != user) {
            throw new AccessDeniedException("User can update items that belong to him/her only");
        }

        if (request.categoryUuid() != null) {
            Category category = categoryService.getActiveCategory(request.categoryUuid());

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
    public ItemSummaryResponse updatePrice(String loggedInUserUuid, String itemUuid, UpdateItemPriceRequest request) {
        User user = userService.findActiveUser(loggedInUserUuid);

        Item item = itemRepository.findByUuidAndStatus(itemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemUuid));

        Store store = storeService.getActiveStoreThatIsGlobalOrBelongsToUser(
                request.createPriceObservationRequest().storeUuid(),
                loggedInUserUuid);

        ownershipAuthorizationService.verifyOwnership(loggedInUserUuid, item.getUser().getUuid());

        PriceObservation lastPriceObservation = priceObservationService.getItemLatestPriceObservation(itemUuid);
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

    public Page<PriceObservationSummaryResponse> getPriceObservationsWithDateRange(String itemUuid, LocalDate dateFrom,
                                                                         LocalDate dateTo, Pageable pageable) {

        Specification<PriceObservation> spec = Specification.where(PriceObservationSpecs.hasItemUuid(itemUuid));

        if (dateFrom != null) {
            spec = spec.and(PriceObservationSpecs.dateFrom(dateFrom));
        }

        if (dateTo != null) {
            spec = spec.and(PriceObservationSpecs.dateTo(dateTo));
        }

        return priceObservationService.getPriceObservationsBasedOnDateRange(spec, pageable)
                .map(PriceObservationSummaryResponse::from);
    }

    public InflationCalculationResponse calculateInflation(String userUuid, String itemUuid, CurrencyEnum currency,
                                                           LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate) || startDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Invalid date range provided");
        }

        Item item = itemRepository.findByUuidAndUser_Uuid(itemUuid, userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "uuid", itemUuid));

        List<PriceObservation> priceObservationsForDateRange = priceObservationService.getAllPricesForItemBasedOnCurrencyAndDateRange(
                item.getUuid(), currency, startDate, endDate);

        if (priceObservationsForDateRange.size() < 2) {
            return InflationCalculationResponse.insufficientData();
        }

        BigDecimal startPrice = priceObservationsForDateRange.getFirst().getPrice();
        BigDecimal endPrice = priceObservationsForDateRange.getLast().getPrice();
        BigDecimal absolutePriceChange = endPrice.subtract(startPrice);
        BigDecimal inflationRate = endPrice.subtract(startPrice)
                .divide(startPrice, 10, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_EVEN);

        return new InflationCalculationResponse(
                startPrice,
                endPrice,
                startDate,
                endDate,
                inflationRate,
                absolutePriceChange,
                currency,
                null
        );
    }

    public Page<ItemSummaryResponse> searchItems(String userUuid, ItemSearchFilterRequest request, Pageable pageable) {
        Specification<Item> spec = Specification.where(ItemSpecs.belongsToUser(userUuid));

        if (request.status() != null) {
            spec = spec.and(ItemSpecs.hasStatus(request.status()));
        }

        if (request.brand() != null) {
            spec = spec.and(ItemSpecs.hasBrand(request.brand()));
        }

        if (request.itemUnit() != null) {
            spec = spec.and(ItemSpecs.hasUnit(request.itemUnit()));
        }

        if (request.isFavorite() != null) {
            spec = spec.and(ItemSpecs.isFavorite());
        }

        if (request.categoryUuid() != null) {
            spec = spec.and(ItemSpecs.inCategory(request.categoryUuid()));
        }

        if (request.createdAfter() != null) {
            spec = spec.and(ItemSpecs.createdAfter(request.createdAfter()));
        }

        if (request.createdBefore() != null) {
            spec = spec.and(ItemSpecs.createdBefore(request.createdBefore()));
        }

        log.debug("Fetching Item through filter search");
        return itemRepository.findAll(spec, pageable).map(ItemSummaryResponse::from);
    }

    public Item getItemByUuidAndUser(String itemUuid, String userUuid) {
        return itemRepository.findByUuidAndUser_Uuid(itemUuid, userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found for this user"));
    }

    public Page<ItemSummaryResponse> getAllItemsForUser(String userUuid, Pageable pageable) {
        Page<Item> results = itemRepository.findAllByUser_UuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode(), pageable);

        return results.map(ItemSummaryResponse::from);
    }
}
