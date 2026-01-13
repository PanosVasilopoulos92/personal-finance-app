# 📈 Epic 4: Price Tracking System

> **Duration:** 2-3 weeks | **Stories:** 4.1, 4.2  
> **Prerequisites:** Complete Epics 1-3 (User Management, Items, Shopping Lists)

---

## 📋 Epic Overview

This epic transforms your app from a simple item tracker into a **price monitoring system**. Users will be able to:
- Record prices at different stores
- Track price history over time
- Set alerts for target prices
- Compare prices across stores

**Why This Matters:** Price tracking is a real-world feature that requires understanding of time-series data, aggregation queries, and event-driven patterns.

---

## Story 4.1: Price History Tracking

**As a** user  
**I want to** record prices for my items at different stores  
**So that** I can track how prices change over time

### Acceptance Criteria

- [x] User can add a price entry for an item
- [x] Price entry includes: store, price, date, currency
- [x] User can view price history for an item
- [x] Price history sorted by date (newest first)
- [x] User can only manage prices for their own items

---

### Step 1: Create the Store Entity

The Store entity represents where prices are observed:

```java
/**
 * Represents a store where items can be purchased.
 * 
 * Business Rules:
 * - Stores can be shared (global) or user-specific
 * - Users can create their own stores
 * - Store names should be unique within scope (global or per user)
 * 
 * Relationships:
 * - MANY-TO-ONE with User (optional - null for global stores)
 * - ONE-TO-MANY with PriceEntry
 */
@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_store_name", columnList = "name"),
    @Index(name = "idx_store_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store extends BaseEntity {
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 500)
    private String website;
    
    @Column(name = "is_global", nullable = false)
    private Boolean global = false;  // Global stores visible to all users
    
    @Column(nullable = false)
    private Boolean active = true;
    
    /**
     * Owner of this store (null for global stores).
     * Optional relationship - global stores have no owner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Price entries recorded at this store.
     * ONE-TO-MANY - Store has many price entries.
     */
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<PriceEntry> priceEntries = new ArrayList<>();
    
    // ========== HELPER METHODS ==========
    
    public boolean isGlobalStore() {
        return Boolean.TRUE.equals(global);
    }
    
    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }
    
    public boolean isAccessibleBy(Long userId) {
        return isGlobalStore() || isOwnedBy(userId);
    }
}
```

---

### Step 2: Create the PriceEntry Entity

The core entity for tracking prices:

```java
/**
 * Represents a single price observation for an item at a store.
 * 
 * Business Rules:
 * - Price must be positive
 * - Each entry has a specific date (when the price was observed)
 * - Currency defaults to user's preferred currency
 * - Notes can include sale info, unit price, etc.
 * 
 * Relationships:
 * - MANY-TO-ONE with Item (required)
 * - MANY-TO-ONE with Store (required)
 * - MANY-TO-ONE with User (required - who recorded this)
 */
@Entity
@Table(name = "price_entries", indexes = {
    @Index(name = "idx_price_item", columnList = "item_id"),
    @Index(name = "idx_price_store", columnList = "store_id"),
    @Index(name = "idx_price_date", columnList = "observed_at"),
    @Index(name = "idx_price_item_date", columnList = "item_id, observed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceEntry extends BaseEntity {
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;
    
    @Column(length = 500)
    private String notes;  // "On sale", "Per kg", etc.
    
    @Column(name = "is_sale")
    private Boolean onSale = false;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;  // If on sale, the regular price
    
    /**
     * The item this price is for.
     * Required relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    /**
     * The store where this price was observed.
     * Required relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    /**
     * User who recorded this price.
     * Required for ownership tracking.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // ========== HELPER METHODS ==========
    
    /**
     * Calculates the discount percentage if on sale.
     */
    public BigDecimal getDiscountPercentage() {
        if (!Boolean.TRUE.equals(onSale) || originalPrice == null || 
            originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(price)
            .divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }
    
    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }
}
```

---

### Step 3: Create DTOs

**Request DTOs:**

```java
/**
 * Request to create a new price entry.
 */
public record CreatePriceEntryRequest(
    @NotNull(message = "Item ID is required")
    Long itemId,
    
    @NotNull(message = "Store ID is required")
    Long storeId,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
    BigDecimal price,
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    String currency,
    
    LocalDateTime observedAt,  // Defaults to now if not provided
    
    @Size(max = 500, message = "Notes must be at most 500 characters")
    String notes,
    
    Boolean onSale,
    
    @Positive(message = "Original price must be positive")
    BigDecimal originalPrice
) {
    public CreatePriceEntryRequest {
        // Set defaults
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
        if (observedAt == null) {
            observedAt = LocalDateTime.now();
        }
        if (onSale == null) {
            onSale = false;
        }
    }
}

/**
 * Request to create a new store.
 */
public record CreateStoreRequest(
    @NotBlank(message = "Store name is required")
    @Size(max = 100, message = "Store name must be at most 100 characters")
    String name,
    
    @Size(max = 500, message = "Address must be at most 500 characters")
    String address,
    
    @URL(message = "Website must be a valid URL")
    @Size(max = 500, message = "Website must be at most 500 characters")
    String website
) {}
```

**Response DTOs:**

```java
/**
 * Response containing price entry details.
 */
public record PriceEntryResponse(
    Long id,
    Long itemId,
    String itemName,
    Long storeId,
    String storeName,
    BigDecimal price,
    String currency,
    LocalDateTime observedAt,
    String notes,
    Boolean onSale,
    BigDecimal originalPrice,
    BigDecimal discountPercentage,
    LocalDateTime createdAt
) {
    public static PriceEntryResponse from(PriceEntry entry) {
        return new PriceEntryResponse(
            entry.getId(),
            entry.getItem().getId(),
            entry.getItem().getName(),
            entry.getStore().getId(),
            entry.getStore().getName(),
            entry.getPrice(),
            entry.getCurrency(),
            entry.getObservedAt(),
            entry.getNotes(),
            entry.getOnSale(),
            entry.getOriginalPrice(),
            entry.getDiscountPercentage(),
            entry.getCreatedAt()
        );
    }
}

/**
 * Response containing store details.
 */
public record StoreResponse(
    Long id,
    String name,
    String address,
    String website,
    Boolean global,
    Boolean active,
    LocalDateTime createdAt
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getName(),
            store.getAddress(),
            store.getWebsite(),
            store.getGlobal(),
            store.getActive(),
            store.getCreatedAt()
        );
    }
}

/**
 * Price history summary for an item.
 */
public record PriceHistoryResponse(
    Long itemId,
    String itemName,
    BigDecimal currentLowestPrice,
    BigDecimal currentHighestPrice,
    BigDecimal averagePrice,
    BigDecimal allTimeLowest,
    BigDecimal allTimeHighest,
    int totalEntries,
    List<PriceEntryResponse> recentEntries
) {}
```

---

### Step 4: Create Repositories

**PriceEntryRepository:**

```java
@Repository
public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {
    
    // ========== BASIC QUERIES ==========
    
    /**
     * Find all price entries for an item, sorted by date.
     */
    List<PriceEntry> findByItemIdOrderByObservedAtDesc(Long itemId);
    
    /**
     * Find price entries for an item with pagination.
     */
    Page<PriceEntry> findByItemId(Long itemId, Pageable pageable);
    
    /**
     * Find a specific price entry owned by user.
     */
    Optional<PriceEntry> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Find all price entries by user.
     */
    Page<PriceEntry> findByUserId(Long userId, Pageable pageable);
    
    // ========== PRICE QUERIES ==========
    
    /**
     * Find lowest price for an item across all stores.
     */
    @Query("""
        SELECT pe FROM PriceEntry pe
        WHERE pe.item.id = :itemId
        AND pe.price = (
            SELECT MIN(p.price) FROM PriceEntry p 
            WHERE p.item.id = :itemId
        )
        ORDER BY pe.observedAt DESC
        """)
    List<PriceEntry> findLowestPriceForItem(@Param("itemId") Long itemId);
    
    /**
     * Find latest price entry for an item at each store.
     */
    @Query("""
        SELECT pe FROM PriceEntry pe
        WHERE pe.item.id = :itemId
        AND pe.observedAt = (
            SELECT MAX(p.observedAt) FROM PriceEntry p
            WHERE p.item.id = :itemId AND p.store.id = pe.store.id
        )
        ORDER BY pe.price ASC
        """)
    List<PriceEntry> findLatestPricesForItem(@Param("itemId") Long itemId);
    
    /**
     * Find price entries for an item within a date range.
     */
    @Query("""
        SELECT pe FROM PriceEntry pe
        WHERE pe.item.id = :itemId
        AND pe.observedAt BETWEEN :startDate AND :endDate
        ORDER BY pe.observedAt DESC
        """)
    List<PriceEntry> findByItemIdAndDateRange(
        @Param("itemId") Long itemId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // ========== AGGREGATION QUERIES ==========
    
    /**
     * Get average price for an item.
     */
    @Query("""
        SELECT AVG(pe.price) FROM PriceEntry pe
        WHERE pe.item.id = :itemId
        """)
    BigDecimal findAveragePriceForItem(@Param("itemId") Long itemId);
    
    /**
     * Get min and max prices for an item.
     */
    @Query("""
        SELECT MIN(pe.price), MAX(pe.price) FROM PriceEntry pe
        WHERE pe.item.id = :itemId
        """)
    Object[] findPriceRangeForItem(@Param("itemId") Long itemId);
    
    /**
     * Count price entries for an item.
     */
    long countByItemId(Long itemId);
    
    // ========== EAGER FETCH QUERIES ==========
    
    /**
     * Find price entries with item and store eagerly loaded.
     */
    @Query("""
        SELECT pe FROM PriceEntry pe
        JOIN FETCH pe.item i
        JOIN FETCH pe.store s
        WHERE pe.item.id = :itemId
        ORDER BY pe.observedAt DESC
        """)
    List<PriceEntry> findByItemIdWithDetails(@Param("itemId") Long itemId);
    
    /**
     * Find price entries for user with all relationships.
     */
    @Query("""
        SELECT pe FROM PriceEntry pe
        JOIN FETCH pe.item i
        JOIN FETCH pe.store s
        WHERE pe.user.id = :userId
        ORDER BY pe.observedAt DESC
        """)
    List<PriceEntry> findByUserIdWithDetails(@Param("userId") Long userId);
}
```

**StoreRepository:**

```java
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * Find all stores accessible by a user (global + user's own).
     */
    @Query("""
        SELECT s FROM Store s
        WHERE s.global = true OR s.user.id = :userId
        ORDER BY s.name
        """)
    List<Store> findAccessibleByUser(@Param("userId") Long userId);
    
    /**
     * Find all global stores.
     */
    List<Store> findByGlobalTrueOrderByName();
    
    /**
     * Find stores owned by a user.
     */
    List<Store> findByUserIdOrderByName(Long userId);
    
    /**
     * Find store by ID if accessible by user.
     */
    @Query("""
        SELECT s FROM Store s
        WHERE s.id = :storeId
        AND (s.global = true OR s.user.id = :userId)
        """)
    Optional<Store> findByIdAndAccessibleByUser(
        @Param("storeId") Long storeId,
        @Param("userId") Long userId
    );
    
    /**
     * Check if store name exists for user.
     */
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
    
    /**
     * Check if global store name exists.
     */
    boolean existsByNameIgnoreCaseAndGlobalTrue(String name);
}
```

---

### Step 5: Create Services

**StoreService:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {
    
    private final StoreRepository storeRepository;
    
    @Transactional(readOnly = true)
    public List<StoreResponse> getAccessibleStores(Long userId) {
        log.debug("Fetching stores accessible by user: {}", userId);
        return storeRepository.findAccessibleByUser(userId)
            .stream()
            .map(StoreResponse::from)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long storeId, Long userId) {
        log.debug("Fetching store {} for user {}", storeId, userId);
        
        Store store = storeRepository.findByIdAndAccessibleByUser(storeId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Store not found or not accessible: " + storeId
            ));
        
        return StoreResponse.from(store);
    }
    
    @Transactional
    public StoreResponse createStore(CreateStoreRequest request, User user) {
        log.info("Creating store '{}' for user {}", request.name(), user.getId());
        
        // Check for duplicate name
        if (storeRepository.existsByNameIgnoreCaseAndUserId(request.name(), user.getId())) {
            throw new DuplicateResourceException(
                "Store with name '" + request.name() + "' already exists"
            );
        }
        
        Store store = new Store();
        store.setName(request.name());
        store.setAddress(request.address());
        store.setWebsite(request.website());
        store.setGlobal(false);  // User-created stores are not global
        store.setUser(user);
        
        Store saved = storeRepository.save(store);
        log.info("Created store with ID: {}", saved.getId());
        
        return StoreResponse.from(saved);
    }
    
    @Transactional
    public void deleteStore(Long storeId, Long userId) {
        log.info("Deleting store {} for user {}", storeId, userId);
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));
        
        // Can't delete global stores
        if (store.isGlobalStore()) {
            throw new BusinessValidationException("Cannot delete global stores");
        }
        
        // Can't delete other user's stores
        if (!store.isOwnedBy(userId)) {
            throw new AccessDeniedException("Not authorized to delete this store");
        }
        
        storeRepository.delete(store);
        log.info("Deleted store: {}", storeId);
    }
}
```

**PriceEntryService:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEntryService {
    
    private final PriceEntryRepository priceEntryRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    
    // ========== CREATE ==========
    
    @Transactional
    public PriceEntryResponse createPriceEntry(CreatePriceEntryRequest request, User user) {
        log.info("Creating price entry for item {} at store {} by user {}",
            request.itemId(), request.storeId(), user.getId());
        
        // Validate item ownership
        Item item = itemRepository.findByIdAndUserId(request.itemId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Item not found or not owned by user: " + request.itemId()
            ));
        
        // Validate store accessibility
        Store store = storeRepository.findByIdAndAccessibleByUser(request.storeId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Store not found or not accessible: " + request.storeId()
            ));
        
        // Create price entry
        PriceEntry entry = new PriceEntry();
        entry.setItem(item);
        entry.setStore(store);
        entry.setUser(user);
        entry.setPrice(request.price());
        entry.setCurrency(request.currency());
        entry.setObservedAt(request.observedAt());
        entry.setNotes(request.notes());
        entry.setOnSale(request.onSale());
        entry.setOriginalPrice(request.originalPrice());
        
        PriceEntry saved = priceEntryRepository.save(entry);
        log.info("Created price entry with ID: {}", saved.getId());
        
        return PriceEntryResponse.from(saved);
    }
    
    // ========== READ ==========
    
    @Transactional(readOnly = true)
    public List<PriceEntryResponse> getPriceHistoryForItem(Long itemId, Long userId) {
        log.debug("Fetching price history for item {} by user {}", itemId, userId);
        
        // Verify item ownership
        itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        return priceEntryRepository.findByItemIdWithDetails(itemId)
            .stream()
            .map(PriceEntryResponse::from)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<PriceEntryResponse> getPriceHistoryPaginated(
            Long itemId, Long userId, Pageable pageable) {
        
        // Verify item ownership
        itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        return priceEntryRepository.findByItemId(itemId, pageable)
            .map(PriceEntryResponse::from);
    }
    
    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistorySummary(Long itemId, Long userId) {
        log.debug("Fetching price history summary for item {}", itemId);
        
        // Verify item ownership
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        // Get statistics
        BigDecimal avgPrice = priceEntryRepository.findAveragePriceForItem(itemId);
        Object[] priceRange = priceEntryRepository.findPriceRangeForItem(itemId);
        long totalEntries = priceEntryRepository.countByItemId(itemId);
        
        // Get current prices (latest at each store)
        List<PriceEntry> latestPrices = priceEntryRepository.findLatestPricesForItem(itemId);
        BigDecimal currentLowest = latestPrices.isEmpty() ? null : 
            latestPrices.stream()
                .map(PriceEntry::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal currentHighest = latestPrices.isEmpty() ? null :
            latestPrices.stream()
                .map(PriceEntry::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(null);
        
        // Get recent entries
        List<PriceEntryResponse> recentEntries = priceEntryRepository
            .findByItemIdWithDetails(itemId)
            .stream()
            .limit(10)
            .map(PriceEntryResponse::from)
            .toList();
        
        return new PriceHistoryResponse(
            itemId,
            item.getName(),
            currentLowest,
            currentHighest,
            avgPrice != null ? avgPrice.setScale(2, RoundingMode.HALF_UP) : null,
            priceRange[0] != null ? (BigDecimal) priceRange[0] : null,
            priceRange[1] != null ? (BigDecimal) priceRange[1] : null,
            (int) totalEntries,
            recentEntries
        );
    }
    
    // ========== DELETE ==========
    
    @Transactional
    public void deletePriceEntry(Long entryId, Long userId) {
        log.info("Deleting price entry {} for user {}", entryId, userId);
        
        PriceEntry entry = priceEntryRepository.findByIdAndUserId(entryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Price entry not found or not owned: " + entryId
            ));
        
        priceEntryRepository.delete(entry);
        log.info("Deleted price entry: {}", entryId);
    }
}
```

---

### Step 6: Create Controllers

**StoreController:**

```java
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    
    private final StoreService storeService;
    
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAccessibleStores(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/stores for user {}", userDetails.getUser().getId());
        
        List<StoreResponse> stores = storeService.getAccessibleStores(
            userDetails.getUser().getId()
        );
        return ResponseEntity.ok(stores);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/stores/{} for user {}", id, userDetails.getUser().getId());
        
        StoreResponse store = storeService.getStoreById(id, userDetails.getUser().getId());
        return ResponseEntity.ok(store);
    }
    
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody CreateStoreRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("POST /api/stores - Creating store: {}", request.name());
        
        StoreResponse store = storeService.createStore(request, userDetails.getUser());
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(store.id())
            .toUri();
        
        return ResponseEntity.created(location).body(store);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("DELETE /api/stores/{}", id);
        
        storeService.deleteStore(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
```

**PriceEntryController:**

```java
@RestController
@RequestMapping("/api/items/{itemId}/prices")
@RequiredArgsConstructor
@Slf4j
public class PriceEntryController {
    
    private final PriceEntryService priceEntryService;
    
    @PostMapping
    public ResponseEntity<PriceEntryResponse> createPriceEntry(
            @PathVariable Long itemId,
            @Valid @RequestBody CreatePriceEntryRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("POST /api/items/{}/prices", itemId);
        
        // Ensure itemId in path matches request
        if (!itemId.equals(request.itemId())) {
            throw new BusinessValidationException(
                "Item ID in path doesn't match request body"
            );
        }
        
        PriceEntryResponse response = priceEntryService.createPriceEntry(
            request, userDetails.getUser()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PriceEntryResponse>> getPriceHistory(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/items/{}/prices", itemId);
        
        List<PriceEntryResponse> history = priceEntryService.getPriceHistoryForItem(
            itemId, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<PriceHistoryResponse> getPriceHistorySummary(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/items/{}/prices/summary", itemId);
        
        PriceHistoryResponse summary = priceEntryService.getPriceHistorySummary(
            itemId, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(summary);
    }
    
    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> deletePriceEntry(
            @PathVariable Long itemId,
            @PathVariable Long priceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("DELETE /api/items/{}/prices/{}", itemId, priceId);
        
        priceEntryService.deletePriceEntry(priceId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
```

---

### Story 4.1 Checklist

Before moving to Story 4.2:
- [ ] Store entity created with proper relationships
- [ ] PriceEntry entity created with proper relationships
- [ ] Request and Response DTOs created
- [ ] StoreRepository with custom queries
- [ ] PriceEntryRepository with aggregation queries
- [ ] StoreService with CRUD operations
- [ ] PriceEntryService with business logic
- [ ] StoreController with REST endpoints
- [ ] PriceEntryController with REST endpoints
- [ ] Unit tests for services
- [ ] Integration tests for controllers

---

## Story 4.2: Price Alerts & Comparisons

**As a** user  
**I want to** set price alerts and compare prices across stores  
**So that** I know when to buy and where to get the best deal

### Acceptance Criteria

- [x] User can set a target price for an item
- [x] System identifies when current price is below target
- [x] User can view price comparison across stores
- [x] User can see price trend (up/down) for an item

---

### Step 1: Add Target Price to Item

Update the Item entity to include target price tracking:

```java
// Add these fields to Item entity
@Column(name = "target_price", precision = 10, scale = 2)
private BigDecimal targetPrice;

@Column(name = "alert_enabled")
private Boolean alertEnabled = false;

// Add helper method
public boolean isPriceBelowTarget(BigDecimal currentPrice) {
    if (targetPrice == null || currentPrice == null) {
        return false;
    }
    return currentPrice.compareTo(targetPrice) <= 0;
}
```

---

### Step 2: Create Price Alert DTOs

```java
/**
 * Request to set a price alert.
 */
public record SetPriceAlertRequest(
    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be positive")
    BigDecimal targetPrice,
    
    @NotNull(message = "Alert enabled flag is required")
    Boolean alertEnabled
) {}

/**
 * Response containing price alert status.
 */
public record PriceAlertResponse(
    Long itemId,
    String itemName,
    BigDecimal targetPrice,
    Boolean alertEnabled,
    BigDecimal currentLowestPrice,
    String lowestPriceStore,
    Boolean alertTriggered,
    BigDecimal savingsAmount,
    BigDecimal savingsPercentage
) {
    public static PriceAlertResponse from(Item item, PriceEntry lowestPrice) {
        BigDecimal savings = null;
        BigDecimal savingsPercent = null;
        boolean triggered = false;
        
        if (item.getTargetPrice() != null && lowestPrice != null) {
            if (lowestPrice.getPrice().compareTo(item.getTargetPrice()) <= 0) {
                triggered = true;
                savings = item.getTargetPrice().subtract(lowestPrice.getPrice());
                savingsPercent = savings
                    .divide(item.getTargetPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
        }
        
        return new PriceAlertResponse(
            item.getId(),
            item.getName(),
            item.getTargetPrice(),
            item.getAlertEnabled(),
            lowestPrice != null ? lowestPrice.getPrice() : null,
            lowestPrice != null ? lowestPrice.getStore().getName() : null,
            triggered,
            savings,
            savingsPercent
        );
    }
}

/**
 * Price comparison across stores.
 */
public record PriceComparisonResponse(
    Long itemId,
    String itemName,
    BigDecimal targetPrice,
    List<StorePriceInfo> storePrices,
    String bestDealStore,
    BigDecimal bestDealPrice,
    BigDecimal priceRange
) {
    public record StorePriceInfo(
        Long storeId,
        String storeName,
        BigDecimal latestPrice,
        LocalDateTime observedAt,
        Boolean isBestDeal,
        BigDecimal differenceFromBest
    ) {}
}

/**
 * Price trend analysis.
 */
public record PriceTrendResponse(
    Long itemId,
    String itemName,
    TrendDirection trend,
    BigDecimal currentPrice,
    BigDecimal previousPrice,
    BigDecimal priceChange,
    BigDecimal percentageChange,
    List<PriceTrendPoint> trendPoints
) {
    public enum TrendDirection {
        UP, DOWN, STABLE
    }
    
    public record PriceTrendPoint(
        LocalDateTime date,
        BigDecimal price,
        String storeName
    ) {}
}
```

---

### Step 3: Update ItemService for Price Alerts

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    
    // ... existing code ...
    
    @Transactional
    public ItemResponse setPriceAlert(Long itemId, SetPriceAlertRequest request, Long userId) {
        log.info("Setting price alert for item {} by user {}", itemId, userId);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        item.setTargetPrice(request.targetPrice());
        item.setAlertEnabled(request.alertEnabled());
        
        // No explicit save needed - dirty checking handles it
        log.info("Price alert set: target={}, enabled={}", 
            request.targetPrice(), request.alertEnabled());
        
        return ItemResponse.from(item);
    }
}
```

---

### Step 4: Create PriceAlertService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertService {
    
    private final ItemRepository itemRepository;
    private final PriceEntryRepository priceEntryRepository;
    
    /**
     * Get alert status for a specific item.
     */
    @Transactional(readOnly = true)
    public PriceAlertResponse getAlertStatus(Long itemId, Long userId) {
        log.debug("Getting alert status for item {}", itemId);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        // Get lowest current price
        List<PriceEntry> lowestPrices = priceEntryRepository.findLowestPriceForItem(itemId);
        PriceEntry lowestPrice = lowestPrices.isEmpty() ? null : lowestPrices.get(0);
        
        return PriceAlertResponse.from(item, lowestPrice);
    }
    
    /**
     * Get all triggered alerts for a user.
     */
    @Transactional(readOnly = true)
    public List<PriceAlertResponse> getTriggeredAlerts(Long userId) {
        log.debug("Getting triggered alerts for user {}", userId);
        
        // Find items with alerts enabled
        List<Item> itemsWithAlerts = itemRepository.findByUserIdAndAlertEnabledTrue(userId);
        
        return itemsWithAlerts.stream()
            .map(item -> {
                List<PriceEntry> lowestPrices = priceEntryRepository
                    .findLowestPriceForItem(item.getId());
                PriceEntry lowestPrice = lowestPrices.isEmpty() ? null : lowestPrices.get(0);
                return PriceAlertResponse.from(item, lowestPrice);
            })
            .filter(alert -> Boolean.TRUE.equals(alert.alertTriggered()))
            .toList();
    }
    
    /**
     * Compare prices across stores for an item.
     */
    @Transactional(readOnly = true)
    public PriceComparisonResponse comparePrices(Long itemId, Long userId) {
        log.debug("Comparing prices for item {}", itemId);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        // Get latest prices at each store
        List<PriceEntry> latestPrices = priceEntryRepository.findLatestPricesForItem(itemId);
        
        if (latestPrices.isEmpty()) {
            return new PriceComparisonResponse(
                itemId, item.getName(), item.getTargetPrice(),
                Collections.emptyList(), null, null, null
            );
        }
        
        // Find best deal
        PriceEntry bestDeal = latestPrices.stream()
            .min(Comparator.comparing(PriceEntry::getPrice))
            .orElse(latestPrices.get(0));
        
        BigDecimal bestPrice = bestDeal.getPrice();
        BigDecimal highestPrice = latestPrices.stream()
            .map(PriceEntry::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(bestPrice);
        
        // Build store price info
        List<PriceComparisonResponse.StorePriceInfo> storePrices = latestPrices.stream()
            .map(entry -> new PriceComparisonResponse.StorePriceInfo(
                entry.getStore().getId(),
                entry.getStore().getName(),
                entry.getPrice(),
                entry.getObservedAt(),
                entry.getPrice().compareTo(bestPrice) == 0,
                entry.getPrice().subtract(bestPrice)
            ))
            .sorted(Comparator.comparing(PriceComparisonResponse.StorePriceInfo::latestPrice))
            .toList();
        
        return new PriceComparisonResponse(
            itemId,
            item.getName(),
            item.getTargetPrice(),
            storePrices,
            bestDeal.getStore().getName(),
            bestPrice,
            highestPrice.subtract(bestPrice)
        );
    }
    
    /**
     * Get price trend for an item.
     */
    @Transactional(readOnly = true)
    public PriceTrendResponse getPriceTrend(Long itemId, Long userId, int days) {
        log.debug("Getting price trend for item {} over {} days", itemId, days);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<PriceEntry> entries = priceEntryRepository.findByItemIdAndDateRange(
            itemId, startDate, endDate
        );
        
        if (entries.isEmpty()) {
            return new PriceTrendResponse(
                itemId, item.getName(),
                PriceTrendResponse.TrendDirection.STABLE,
                null, null, null, null,
                Collections.emptyList()
            );
        }
        
        // Sort by date ascending for trend calculation
        entries.sort(Comparator.comparing(PriceEntry::getObservedAt));
        
        PriceEntry oldest = entries.get(0);
        PriceEntry newest = entries.get(entries.size() - 1);
        
        BigDecimal priceChange = newest.getPrice().subtract(oldest.getPrice());
        BigDecimal percentChange = priceChange
            .divide(oldest.getPrice(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        PriceTrendResponse.TrendDirection trend;
        if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
            trend = PriceTrendResponse.TrendDirection.UP;
        } else if (priceChange.compareTo(BigDecimal.ZERO) < 0) {
            trend = PriceTrendResponse.TrendDirection.DOWN;
        } else {
            trend = PriceTrendResponse.TrendDirection.STABLE;
        }
        
        List<PriceTrendResponse.PriceTrendPoint> trendPoints = entries.stream()
            .map(e -> new PriceTrendResponse.PriceTrendPoint(
                e.getObservedAt(),
                e.getPrice(),
                e.getStore().getName()
            ))
            .toList();
        
        return new PriceTrendResponse(
            itemId,
            item.getName(),
            trend,
            newest.getPrice(),
            oldest.getPrice(),
            priceChange,
            percentChange,
            trendPoints
        );
    }
}
```

---

### Step 5: Create PriceAlertController

```java
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class PriceAlertController {
    
    private final PriceAlertService priceAlertService;
    private final ItemService itemService;
    
    @GetMapping("/triggered")
    public ResponseEntity<List<PriceAlertResponse>> getTriggeredAlerts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/alerts/triggered");
        
        List<PriceAlertResponse> alerts = priceAlertService.getTriggeredAlerts(
            userDetails.getUser().getId()
        );
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/items/{itemId}")
    public ResponseEntity<PriceAlertResponse> getAlertStatus(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/alerts/items/{}", itemId);
        
        PriceAlertResponse alert = priceAlertService.getAlertStatus(
            itemId, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(alert);
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> setPriceAlert(
            @PathVariable Long itemId,
            @Valid @RequestBody SetPriceAlertRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("PUT /api/alerts/items/{}", itemId);
        
        ItemResponse item = itemService.setPriceAlert(
            itemId, request, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(item);
    }
    
    @GetMapping("/items/{itemId}/compare")
    public ResponseEntity<PriceComparisonResponse> comparePrices(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/alerts/items/{}/compare", itemId);
        
        PriceComparisonResponse comparison = priceAlertService.comparePrices(
            itemId, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(comparison);
    }
    
    @GetMapping("/items/{itemId}/trend")
    public ResponseEntity<PriceTrendResponse> getPriceTrend(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("GET /api/alerts/items/{}/trend?days={}", itemId, days);
        
        PriceTrendResponse trend = priceAlertService.getPriceTrend(
            itemId, userDetails.getUser().getId(), days
        );
        return ResponseEntity.ok(trend);
    }
}
```

---

### Story 4.2 Checklist

Before completing Epic 4:
- [ ] Target price field added to Item entity
- [ ] SetPriceAlertRequest DTO created
- [ ] PriceAlertResponse DTO created
- [ ] PriceComparisonResponse DTO created
- [ ] PriceTrendResponse DTO created
- [ ] ItemRepository query for items with alerts enabled
- [ ] PriceAlertService with all business logic
- [ ] PriceAlertController with REST endpoints
- [ ] Unit tests for PriceAlertService
- [ ] Integration tests for alerts and comparisons

---

## 📊 Testing Price Tracking Features

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private PriceEntryRepository priceEntryRepository;
    
    @InjectMocks
    private PriceAlertService priceAlertService;
    
    private User testUser;
    private Item testItem;
    private PriceEntry testPriceEntry;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setTargetPrice(new BigDecimal("10.00"));
        testItem.setAlertEnabled(true);
        testItem.setUser(testUser);
        
        Store testStore = new Store();
        testStore.setId(1L);
        testStore.setName("Test Store");
        
        testPriceEntry = new PriceEntry();
        testPriceEntry.setId(1L);
        testPriceEntry.setItem(testItem);
        testPriceEntry.setStore(testStore);
        testPriceEntry.setPrice(new BigDecimal("8.50"));
        testPriceEntry.setUser(testUser);
    }
    
    @Test
    @DisplayName("getAlertStatus - when price below target - should return triggered alert")
    void getAlertStatus_WhenPriceBelowTarget_ReturnsTriggeredAlert() {
        // Arrange
        when(itemRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(testItem));
        when(priceEntryRepository.findLowestPriceForItem(1L))
            .thenReturn(List.of(testPriceEntry));
        
        // Act
        PriceAlertResponse response = priceAlertService.getAlertStatus(1L, 1L);
        
        // Assert
        assertThat(response.alertTriggered()).isTrue();
        assertThat(response.currentLowestPrice()).isEqualTo(new BigDecimal("8.50"));
        assertThat(response.savingsAmount()).isEqualTo(new BigDecimal("1.50"));
    }
    
    @Test
    @DisplayName("getAlertStatus - when price above target - should not trigger alert")
    void getAlertStatus_WhenPriceAboveTarget_DoesNotTriggerAlert() {
        // Arrange
        testPriceEntry.setPrice(new BigDecimal("12.00"));
        
        when(itemRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(testItem));
        when(priceEntryRepository.findLowestPriceForItem(1L))
            .thenReturn(List.of(testPriceEntry));
        
        // Act
        PriceAlertResponse response = priceAlertService.getAlertStatus(1L, 1L);
        
        // Assert
        assertThat(response.alertTriggered()).isFalse();
        assertThat(response.savingsAmount()).isNull();
    }
}
```

---

## 🎯 Epic 4 Summary

### What You've Learned

1. **Time-series data modeling** - PriceEntry entity with temporal queries
2. **Aggregation queries** - MIN, MAX, AVG calculations in JPQL
3. **Multi-entity relationships** - Item → PriceEntry ← Store
4. **Business intelligence** - Trend analysis and comparisons
5. **Alert systems** - Target-based notifications

### API Endpoints Created

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stores` | List accessible stores |
| POST | `/api/stores` | Create store |
| DELETE | `/api/stores/{id}` | Delete store |
| POST | `/api/items/{id}/prices` | Add price entry |
| GET | `/api/items/{id}/prices` | Get price history |
| GET | `/api/items/{id}/prices/summary` | Get price summary |
| GET | `/api/alerts/triggered` | Get triggered alerts |
| PUT | `/api/alerts/items/{id}` | Set price alert |
| GET | `/api/alerts/items/{id}/compare` | Compare prices |
| GET | `/api/alerts/items/{id}/trend` | Get price trend |

---

**Next:** [06-Epic5-Testing-Strategy.md](./06-Epic5-Testing-Strategy.md) - Build comprehensive test coverage
