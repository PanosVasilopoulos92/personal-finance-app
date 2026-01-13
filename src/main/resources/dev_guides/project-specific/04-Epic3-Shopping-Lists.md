# 📦 Epic 3: Shopping List Management

> **Duration:** 2 weeks  
> **Goal:** Enable users to organize items into purchasable shopping lists

---

## Epic Overview

| Story | Title | Days | Status |
|-------|-------|------|--------|
| 3.1 | Shopping List CRUD | 3 | ⬜ |
| 3.2 | Add Items to Shopping List | 4 | ⬜ |

---

# Story 3.1: Shopping List CRUD

## User Story

**As a** user  
**I want to** create shopping lists  
**So that** I can organize items I plan to purchase

## Acceptance Criteria

- [ ] User can create multiple shopping lists
- [ ] List has name, optional description
- [ ] List can be archived
- [ ] User can only manage their own lists
- [ ] List names unique per user

---

## Task 3.1.1: Create ShoppingList Entity

```java
package com.yourname.personalfinance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Shopping list for organizing items to purchase.
 */
@Entity
@Table(name = "shopping_lists", indexes = {
    @Index(name = "idx_shopping_list_user", columnList = "user_id"),
    @Index(name = "idx_shopping_list_user_name", columnList = "user_id, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingList extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean favorite = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShoppingListItem> items = new ArrayList<>();
    
    /**
     * Calculates total number of items.
     */
    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }
    
    /**
     * Calculates number of purchased items.
     */
    public int getPurchasedCount() {
        if (items == null) return 0;
        return (int) items.stream()
            .filter(item -> item.getPurchasedAt() != null)
            .count();
    }
    
    /**
     * Checks if all items are purchased.
     */
    public boolean isComplete() {
        return !items.isEmpty() && getTotalItems() == getPurchasedCount();
    }
}
```

---

## Task 3.1.2: Create ShoppingList DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record CreateShoppingListRequest(
    @NotBlank(message = "List name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,
    
    Boolean favorite
) {}
```

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record UpdateShoppingListRequest(
    @Size(max = 100, message = "Name must be at most 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,
    
    Boolean favorite,
    
    Boolean archived
) {}
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.ShoppingList;
import java.time.LocalDateTime;

public record ShoppingListResponse(
    Long id,
    String name,
    String description,
    Boolean favorite,
    Boolean archived,
    Integer totalItems,
    Integer purchasedCount,
    Boolean isComplete,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShoppingListResponse from(ShoppingList list) {
        return new ShoppingListResponse(
            list.getId(),
            list.getName(),
            list.getDescription(),
            list.getFavorite(),
            list.getArchived(),
            list.getTotalItems(),
            list.getPurchasedCount(),
            list.isComplete(),
            list.getCreatedAt(),
            list.getUpdatedAt()
        );
    }
}
```

---

## Task 3.1.3: Create ShoppingListRepository

```java
package com.yourname.personalfinance.repository;

import com.yourname.personalfinance.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    
    List<ShoppingList> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(Long userId);
    
    List<ShoppingList> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<ShoppingList> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    
    @Query("SELECT COUNT(sl) > 0 FROM ShoppingList sl " +
           "WHERE sl.user.id = :userId AND LOWER(sl.name) = LOWER(:name) AND sl.id != :excludeId")
    boolean existsByUserIdAndNameIgnoreCaseExcluding(
        @Param("userId") Long userId,
        @Param("name") String name,
        @Param("excludeId") Long excludeId
    );
    
    /**
     * Loads shopping list with items eagerly.
     */
    @Query("SELECT sl FROM ShoppingList sl " +
           "LEFT JOIN FETCH sl.items sli " +
           "LEFT JOIN FETCH sli.item " +
           "WHERE sl.id = :id AND sl.user.id = :userId")
    Optional<ShoppingList> findByIdAndUserIdWithItems(
        @Param("id") Long id, @Param("userId") Long userId);
    
    List<ShoppingList> findByUserIdAndFavoriteTrueAndArchivedFalse(Long userId);
}
```

---

## Task 3.1.4: Create ShoppingListService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.CreateShoppingListRequest;
import com.yourname.personalfinance.dto.request.UpdateShoppingListRequest;
import com.yourname.personalfinance.dto.response.ShoppingListResponse;
import com.yourname.personalfinance.entity.ShoppingList;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.exception.*;
import com.yourname.personalfinance.repository.ShoppingListRepository;
import com.yourname.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {
    
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ShoppingListResponse createShoppingList(Long userId, CreateShoppingListRequest request) {
        log.info("Creating shopping list '{}' for user {}", request.name(), userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        if (shoppingListRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new DuplicateResourceException("Shopping list already exists: " + request.name());
        }
        
        ShoppingList list = ShoppingList.builder()
            .user(user)
            .name(request.name())
            .description(request.description())
            .favorite(request.favorite() != null ? request.favorite() : false)
            .build();
        
        ShoppingList saved = shoppingListRepository.save(list);
        return ShoppingListResponse.from(saved);
    }
    
    @Transactional(readOnly = true)
    public List<ShoppingListResponse> getAllShoppingLists(Long userId, boolean includeArchived) {
        List<ShoppingList> lists = includeArchived
            ? shoppingListRepository.findByUserIdOrderByCreatedAtDesc(userId)
            : shoppingListRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(userId);
        
        return lists.stream()
            .map(ShoppingListResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ShoppingListResponse getShoppingListById(Long listId, Long userId) {
        ShoppingList list = getListOwnedByUser(listId, userId);
        return ShoppingListResponse.from(list);
    }
    
    @Transactional
    public ShoppingListResponse updateShoppingList(Long listId, Long userId, UpdateShoppingListRequest request) {
        log.info("Updating shopping list {} for user {}", listId, userId);
        
        ShoppingList list = getListOwnedByUser(listId, userId);
        
        if (request.name() != null && !request.name().equalsIgnoreCase(list.getName())) {
            if (shoppingListRepository.existsByUserIdAndNameIgnoreCaseExcluding(
                    userId, request.name(), listId)) {
                throw new DuplicateResourceException("Shopping list already exists: " + request.name());
            }
            list.setName(request.name());
        }
        
        if (request.description() != null) list.setDescription(request.description());
        if (request.favorite() != null) list.setFavorite(request.favorite());
        if (request.archived() != null) list.setArchived(request.archived());
        
        return ShoppingListResponse.from(list);
    }
    
    @Transactional
    public void deleteShoppingList(Long listId, Long userId) {
        log.info("Deleting shopping list {} for user {}", listId, userId);
        
        ShoppingList list = getListOwnedByUser(listId, userId);
        shoppingListRepository.delete(list);
    }
    
    private ShoppingList getListOwnedByUser(Long listId, Long userId) {
        return shoppingListRepository.findByIdAndUserId(listId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found: " + listId));
    }
}
```

---

## Task 3.1.5: Create ShoppingListController

```java
package com.yourname.personalfinance.controller;

import com.yourname.personalfinance.dto.request.CreateShoppingListRequest;
import com.yourname.personalfinance.dto.request.UpdateShoppingListRequest;
import com.yourname.personalfinance.dto.response.ShoppingListResponse;
import com.yourname.personalfinance.security.UserDetailsImpl;
import com.yourname.personalfinance.service.ShoppingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {
    
    private final ShoppingListService shoppingListService;
    
    @PostMapping
    public ResponseEntity<ShoppingListResponse> createShoppingList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateShoppingListRequest request) {
        
        Long userId = userDetails.getUser().getId();
        ShoppingListResponse response = shoppingListService.createShoppingList(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ShoppingListResponse>> getAllShoppingLists(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        Long userId = userDetails.getUser().getId();
        List<ShoppingListResponse> lists = shoppingListService.getAllShoppingLists(userId, includeArchived);
        return ResponseEntity.ok(lists);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> getShoppingList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        ShoppingListResponse response = shoppingListService.getShoppingListById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> updateShoppingList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateShoppingListRequest request) {
        
        Long userId = userDetails.getUser().getId();
        ShoppingListResponse response = shoppingListService.updateShoppingList(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        shoppingListService.deleteShoppingList(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Story 3.1 Checklist

- [ ] ShoppingList entity created
- [ ] ShoppingList DTOs created
- [ ] ShoppingListRepository created
- [ ] ShoppingListService with CRUD
- [ ] ShoppingListController with endpoints
- [ ] Unique name per user validation
- [ ] Unit tests passing

---

# Story 3.2: Add Items to Shopping List

## User Story

**As a** user  
**I want to** add items to my shopping list  
**So that** I can track what I need to buy

## Acceptance Criteria

- [ ] Items can be added to multiple shopping lists
- [ ] Each item in list has quantity and unit
- [ ] Items can be marked as purchased
- [ ] User can only add their own items
- [ ] Item removal doesn't delete the item itself

---

## Task 3.2.1: Create ShoppingListItem Entity (Join Table)

```java
package com.yourname.personalfinance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join table between ShoppingList and Item.
 * Stores additional data: quantity, unit, purchased status.
 */
@Entity
@Table(name = "shopping_list_items", indexes = {
    @Index(name = "idx_sli_list", columnList = "shopping_list_id"),
    @Index(name = "idx_sli_item", columnList = "item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(length = 20)
    private String unit;  // e.g., "kg", "pcs", "lbs"
    
    @Column
    private LocalDateTime purchasedAt;  // null = not purchased
    
    @Column(length = 200)
    private String notes;
    
    /**
     * Marks item as purchased.
     */
    public void markPurchased() {
        this.purchasedAt = LocalDateTime.now();
    }
    
    /**
     * Marks item as not purchased.
     */
    public void markNotPurchased() {
        this.purchasedAt = null;
    }
    
    /**
     * Checks if item is purchased.
     */
    public boolean isPurchased() {
        return purchasedAt != null;
    }
}
```

---

## Task 3.2.2: Create ShoppingListItem DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record AddItemToListRequest(
    @NotNull(message = "Item ID is required")
    Long itemId,
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 9999, message = "Quantity too large")
    Integer quantity,
    
    @Size(max = 20, message = "Unit must be at most 20 characters")
    String unit,
    
    @Size(max = 200, message = "Notes must be at most 200 characters")
    String notes
) {}
```

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record UpdateListItemRequest(
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 9999, message = "Quantity too large")
    Integer quantity,
    
    @Size(max = 20, message = "Unit must be at most 20 characters")
    String unit,
    
    @Size(max = 200, message = "Notes must be at most 200 characters")
    String notes
) {}
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.ShoppingListItem;
import java.time.LocalDateTime;

public record ShoppingListItemResponse(
    Long id,
    Long itemId,
    String itemName,
    String itemBrand,
    Integer quantity,
    String unit,
    Boolean purchased,
    LocalDateTime purchasedAt,
    String notes,
    LocalDateTime createdAt
) {
    public static ShoppingListItemResponse from(ShoppingListItem sli) {
        return new ShoppingListItemResponse(
            sli.getId(),
            sli.getItem().getId(),
            sli.getItem().getName(),
            sli.getItem().getBrand(),
            sli.getQuantity(),
            sli.getUnit(),
            sli.isPurchased(),
            sli.getPurchasedAt(),
            sli.getNotes(),
            sli.getCreatedAt()
        );
    }
}
```

---

## Task 3.2.3: Create ShoppingListItemRepository

```java
package com.yourname.personalfinance.repository;

import com.yourname.personalfinance.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    
    /**
     * Finds all items in a shopping list.
     */
    @Query("SELECT sli FROM ShoppingListItem sli " +
           "JOIN FETCH sli.item " +
           "WHERE sli.shoppingList.id = :listId " +
           "ORDER BY sli.createdAt")
    List<ShoppingListItem> findByShoppingListIdWithItem(@Param("listId") Long listId);
    
    /**
     * Finds specific item in list.
     */
    Optional<ShoppingListItem> findByIdAndShoppingListId(Long id, Long listId);
    
    /**
     * Checks if item already exists in list.
     */
    boolean existsByShoppingListIdAndItemId(Long listId, Long itemId);
    
    /**
     * Deletes item from list.
     */
    void deleteByShoppingListIdAndItemId(Long listId, Long itemId);
    
    /**
     * Counts unpurchased items.
     */
    long countByShoppingListIdAndPurchasedAtIsNull(Long listId);
}
```

---

## Task 3.2.4: Add Methods to ShoppingListService

```java
// Add to ShoppingListService.java

private final ShoppingListItemRepository listItemRepository;
private final ItemRepository itemRepository;

/**
 * Adds an item to a shopping list.
 */
@Transactional
public ShoppingListItemResponse addItemToList(Long listId, Long userId, AddItemToListRequest request) {
    log.info("Adding item {} to list {} for user {}", request.itemId(), listId, userId);
    
    // Verify list ownership
    ShoppingList list = getListOwnedByUser(listId, userId);
    
    // Verify item ownership
    Item item = itemRepository.findByIdAndUserId(request.itemId(), userId)
        .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.itemId()));
    
    // Check if already in list
    if (listItemRepository.existsByShoppingListIdAndItemId(listId, request.itemId())) {
        throw new DuplicateResourceException("Item already in this shopping list");
    }
    
    ShoppingListItem listItem = ShoppingListItem.builder()
        .shoppingList(list)
        .item(item)
        .quantity(request.quantity() != null ? request.quantity() : 1)
        .unit(request.unit())
        .notes(request.notes())
        .build();
    
    ShoppingListItem saved = listItemRepository.save(listItem);
    return ShoppingListItemResponse.from(saved);
}

/**
 * Gets all items in a shopping list.
 */
@Transactional(readOnly = true)
public List<ShoppingListItemResponse> getListItems(Long listId, Long userId) {
    // Verify ownership
    getListOwnedByUser(listId, userId);
    
    return listItemRepository.findByShoppingListIdWithItem(listId)
        .stream()
        .map(ShoppingListItemResponse::from)
        .collect(Collectors.toList());
}

/**
 * Updates item in list.
 */
@Transactional
public ShoppingListItemResponse updateListItem(Long listId, Long itemId, Long userId, 
                                               UpdateListItemRequest request) {
    getListOwnedByUser(listId, userId);
    
    ShoppingListItem listItem = listItemRepository.findByIdAndShoppingListId(itemId, listId)
        .orElseThrow(() -> new ResourceNotFoundException("Item not in list: " + itemId));
    
    if (request.quantity() != null) listItem.setQuantity(request.quantity());
    if (request.unit() != null) listItem.setUnit(request.unit());
    if (request.notes() != null) listItem.setNotes(request.notes());
    
    return ShoppingListItemResponse.from(listItem);
}

/**
 * Marks item as purchased.
 */
@Transactional
public ShoppingListItemResponse markItemPurchased(Long listId, Long itemId, Long userId) {
    getListOwnedByUser(listId, userId);
    
    ShoppingListItem listItem = listItemRepository.findByIdAndShoppingListId(itemId, listId)
        .orElseThrow(() -> new ResourceNotFoundException("Item not in list: " + itemId));
    
    listItem.markPurchased();
    return ShoppingListItemResponse.from(listItem);
}

/**
 * Marks item as not purchased.
 */
@Transactional
public ShoppingListItemResponse markItemNotPurchased(Long listId, Long itemId, Long userId) {
    getListOwnedByUser(listId, userId);
    
    ShoppingListItem listItem = listItemRepository.findByIdAndShoppingListId(itemId, listId)
        .orElseThrow(() -> new ResourceNotFoundException("Item not in list: " + itemId));
    
    listItem.markNotPurchased();
    return ShoppingListItemResponse.from(listItem);
}

/**
 * Removes item from list.
 */
@Transactional
public void removeItemFromList(Long listId, Long itemId, Long userId) {
    getListOwnedByUser(listId, userId);
    
    ShoppingListItem listItem = listItemRepository.findByIdAndShoppingListId(itemId, listId)
        .orElseThrow(() -> new ResourceNotFoundException("Item not in list: " + itemId));
    
    listItemRepository.delete(listItem);
}
```

---

## Task 3.2.5: Add Endpoints to ShoppingListController

```java
// Add to ShoppingListController.java

/**
 * Adds item to shopping list.
 * POST /api/shopping-lists/{listId}/items
 */
@PostMapping("/{listId}/items")
public ResponseEntity<ShoppingListItemResponse> addItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId,
        @Valid @RequestBody AddItemToListRequest request) {
    
    Long userId = userDetails.getUser().getId();
    ShoppingListItemResponse response = shoppingListService.addItemToList(listId, userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

/**
 * Gets all items in shopping list.
 * GET /api/shopping-lists/{listId}/items
 */
@GetMapping("/{listId}/items")
public ResponseEntity<List<ShoppingListItemResponse>> getListItems(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId) {
    
    Long userId = userDetails.getUser().getId();
    List<ShoppingListItemResponse> items = shoppingListService.getListItems(listId, userId);
    return ResponseEntity.ok(items);
}

/**
 * Updates item in shopping list.
 * PUT /api/shopping-lists/{listId}/items/{itemId}
 */
@PutMapping("/{listId}/items/{itemId}")
public ResponseEntity<ShoppingListItemResponse> updateListItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId,
        @PathVariable Long itemId,
        @Valid @RequestBody UpdateListItemRequest request) {
    
    Long userId = userDetails.getUser().getId();
    ShoppingListItemResponse response = shoppingListService.updateListItem(listId, itemId, userId, request);
    return ResponseEntity.ok(response);
}

/**
 * Marks item as purchased.
 * POST /api/shopping-lists/{listId}/items/{itemId}/purchase
 */
@PostMapping("/{listId}/items/{itemId}/purchase")
public ResponseEntity<ShoppingListItemResponse> purchaseItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId,
        @PathVariable Long itemId) {
    
    Long userId = userDetails.getUser().getId();
    ShoppingListItemResponse response = shoppingListService.markItemPurchased(listId, itemId, userId);
    return ResponseEntity.ok(response);
}

/**
 * Marks item as not purchased.
 * DELETE /api/shopping-lists/{listId}/items/{itemId}/purchase
 */
@DeleteMapping("/{listId}/items/{itemId}/purchase")
public ResponseEntity<ShoppingListItemResponse> unpurchaseItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId,
        @PathVariable Long itemId) {
    
    Long userId = userDetails.getUser().getId();
    ShoppingListItemResponse response = shoppingListService.markItemNotPurchased(listId, itemId, userId);
    return ResponseEntity.ok(response);
}

/**
 * Removes item from shopping list.
 * DELETE /api/shopping-lists/{listId}/items/{itemId}
 */
@DeleteMapping("/{listId}/items/{itemId}")
public ResponseEntity<Void> removeItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long listId,
        @PathVariable Long itemId) {
    
    Long userId = userDetails.getUser().getId();
    shoppingListService.removeItemFromList(listId, itemId, userId);
    return ResponseEntity.noContent().build();
}
```

---

## Story 3.2 Checklist

- [ ] ShoppingListItem entity created
- [ ] ShoppingListItem DTOs created
- [ ] ShoppingListItemRepository created
- [ ] Add/remove items methods in service
- [ ] Purchase/unpurchase methods in service
- [ ] All endpoints in controller
- [ ] Ownership validation for both list and item
- [ ] Unit tests passing

---

## Epic 3 Complete Checklist

Before moving to Epic 4:

- [ ] **Story 3.1:** Shopping list CRUD working
- [ ] **Story 3.2:** Add/remove items working
- [ ] Purchase tracking working
- [ ] All ownership validations
- [ ] Unit tests passing
- [ ] Integration tests passing

---

## Next Steps

→ Continue to [05-Epic4-Price-Tracking.md](./05-Epic4-Price-Tracking.md)
