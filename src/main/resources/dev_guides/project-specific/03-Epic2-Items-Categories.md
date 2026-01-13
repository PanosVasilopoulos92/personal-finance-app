# 📦 Epic 2: Item & Category Management

> **Duration:** 2-3 weeks  
> **Goal:** Build the core domain for tracking items organized by categories

---

## Epic Overview

| Story | Title | Days | Status |
|-------|-------|------|--------|
| 2.1 | Category Management | 3 | ⬜ |
| 2.2 | Item Creation & Management | 4 | ⬜ |
| 2.3 | Item Search & Filtering | 3 | ⬜ |

---

# Story 2.1: Category Management

## User Story

**As a** user  
**I want to** organize items into categories  
**So that** I can better track different types of products

## Acceptance Criteria

- [ ] User can create custom categories
- [ ] Category has name, description, optional icon
- [ ] User can only see/manage their own categories
- [ ] Categories can be archived (not deleted if items exist)
- [ ] Category names unique per user

---

## Task 2.1.1: Create Category Entity

```java
package com.yourname.personalfinance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Category for organizing items.
 * 
 * Business Rules:
 * - Category belongs to one user
 * - Name must be unique per user
 * - Cannot delete if items exist (archive instead)
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_user", columnList = "user_id"),
    @Index(name = "idx_category_user_name", columnList = "user_id, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 50)
    private String icon;  // Icon name or emoji
    
    @Column(length = 7)
    private String color;  // Hex color code
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Item> items = new ArrayList<>();
    
    /**
     * Checks if category can be deleted.
     * Cannot delete if items exist.
     */
    public boolean canDelete() {
        return items == null || items.isEmpty();
    }
}
```

---

## Task 2.1.2: Create Category DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record CreateCategoryRequest(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,
    
    @Size(max = 50, message = "Icon must be at most 50 characters")
    String icon,
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be valid hex (e.g., #FF5733)")
    String color
) {}
```

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record UpdateCategoryRequest(
    @Size(max = 100, message = "Name must be at most 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,
    
    @Size(max = 50, message = "Icon must be at most 50 characters")
    String icon,
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be valid hex")
    String color,
    
    Boolean archived
) {}
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.Category;
import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    String icon,
    String color,
    Boolean archived,
    Integer itemCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIcon(),
            category.getColor(),
            category.getArchived(),
            category.getItems() != null ? category.getItems().size() : 0,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
    
    // Overload for when items not loaded
    public static CategoryResponse from(Category category, int itemCount) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIcon(),
            category.getColor(),
            category.getArchived(),
            itemCount,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
```

---

## Task 2.1.3: Create CategoryRepository

```java
package com.yourname.personalfinance.repository;

import com.yourname.personalfinance.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Finds all categories for a user.
     */
    List<Category> findByUserIdOrderByNameAsc(Long userId);
    
    /**
     * Finds non-archived categories for a user.
     */
    List<Category> findByUserIdAndArchivedFalseOrderByNameAsc(Long userId);
    
    /**
     * Finds category by ID and user ID (security check).
     */
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Checks if category name exists for user.
     */
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    
    /**
     * Checks if category name exists for user (excluding specific category).
     * Used for updates to allow keeping same name.
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c " +
           "WHERE c.user.id = :userId AND LOWER(c.name) = LOWER(:name) AND c.id != :excludeId")
    boolean existsByUserIdAndNameIgnoreCaseExcluding(
        @Param("userId") Long userId,
        @Param("name") String name,
        @Param("excludeId") Long excludeId
    );
    
    /**
     * Gets category with item count (prevents N+1).
     */
    @Query("SELECT c, COUNT(i) FROM Category c " +
           "LEFT JOIN c.items i " +
           "WHERE c.user.id = :userId " +
           "GROUP BY c " +
           "ORDER BY c.name")
    List<Object[]> findAllWithItemCount(@Param("userId") Long userId);
}
```

---

## Task 2.1.4: Create CategoryService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.CreateCategoryRequest;
import com.yourname.personalfinance.dto.request.UpdateCategoryRequest;
import com.yourname.personalfinance.dto.response.CategoryResponse;
import com.yourname.personalfinance.entity.Category;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.exception.*;
import com.yourname.personalfinance.repository.CategoryRepository;
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
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    /**
     * Creates a new category for user.
     */
    @Transactional
    public CategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
        log.info("Creating category '{}' for user {}", request.name(), userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        // Check for duplicate name
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new DuplicateResourceException(
                "Category already exists: " + request.name()
            );
        }
        
        // Create category
        Category category = Category.builder()
            .user(user)
            .name(request.name())
            .description(request.description())
            .icon(request.icon())
            .color(request.color())
            .build();
        
        Category saved = categoryRepository.save(category);
        log.info("Category created with id: {}", saved.getId());
        
        return CategoryResponse.from(saved, 0);
    }
    
    /**
     * Gets all categories for user.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long userId, boolean includeArchived) {
        List<Category> categories = includeArchived
            ? categoryRepository.findByUserIdOrderByNameAsc(userId)
            : categoryRepository.findByUserIdAndArchivedFalseOrderByNameAsc(userId);
        
        return categories.stream()
            .map(CategoryResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets category by ID (with ownership check).
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId, Long userId) {
        Category category = getCategoryOwnedByUser(categoryId, userId);
        return CategoryResponse.from(category);
    }
    
    /**
     * Updates a category.
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long userId, UpdateCategoryRequest request) {
        log.info("Updating category {} for user {}", categoryId, userId);
        
        Category category = getCategoryOwnedByUser(categoryId, userId);
        
        // Check name uniqueness if changing name
        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseExcluding(
                    userId, request.name(), categoryId)) {
                throw new DuplicateResourceException(
                    "Category already exists: " + request.name()
                );
            }
            category.setName(request.name());
        }
        
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.icon() != null) {
            category.setIcon(request.icon());
        }
        if (request.color() != null) {
            category.setColor(request.color());
        }
        if (request.archived() != null) {
            category.setArchived(request.archived());
        }
        
        return CategoryResponse.from(category);
    }
    
    /**
     * Deletes a category.
     * Only allowed if no items exist.
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        log.info("Deleting category {} for user {}", categoryId, userId);
        
        Category category = getCategoryOwnedByUser(categoryId, userId);
        
        if (!category.canDelete()) {
            throw new BusinessValidationException(
                "Cannot delete category with existing items. Archive it instead."
            );
        }
        
        categoryRepository.delete(category);
        log.info("Category {} deleted", categoryId);
    }
    
    /**
     * Helper: Gets category and verifies ownership.
     */
    private Category getCategoryOwnedByUser(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found: " + categoryId
            ));
    }
}
```

---

## Task 2.1.5: Create CategoryController

```java
package com.yourname.personalfinance.controller;

import com.yourname.personalfinance.dto.request.CreateCategoryRequest;
import com.yourname.personalfinance.dto.request.UpdateCategoryRequest;
import com.yourname.personalfinance.dto.response.CategoryResponse;
import com.yourname.personalfinance.security.UserDetailsImpl;
import com.yourname.personalfinance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category management endpoints.
 * 
 * All endpoints are secured and scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Creates a new category.
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateCategoryRequest request) {
        
        Long userId = userDetails.getUser().getId();
        log.info("POST /api/categories - user: {}, name: {}", userId, request.name());
        
        CategoryResponse response = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Gets all categories for user.
     * GET /api/categories?includeArchived=false
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        Long userId = userDetails.getUser().getId();
        log.info("GET /api/categories - user: {}", userId);
        
        List<CategoryResponse> categories = categoryService.getAllCategories(userId, includeArchived);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Gets a specific category.
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        log.info("GET /api/categories/{} - user: {}", id, userId);
        
        CategoryResponse response = categoryService.getCategoryById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates a category.
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        Long userId = userDetails.getUser().getId();
        log.info("PUT /api/categories/{} - user: {}", id, userId);
        
        CategoryResponse response = categoryService.updateCategory(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deletes a category.
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        log.info("DELETE /api/categories/{} - user: {}", id, userId);
        
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Story 2.1 Checklist

- [ ] Category entity created
- [ ] Category DTOs created (Create, Update, Response)
- [ ] CategoryRepository with query methods
- [ ] CategoryService with CRUD operations
- [ ] CategoryController with REST endpoints
- [ ] Unique name per user validation
- [ ] Cannot delete category with items
- [ ] Unit tests for CategoryService
- [ ] Integration tests for CategoryController

---

# Story 2.2: Item Creation & Management

## User Story

**As a** user  
**I want to** add items I'm tracking  
**So that** I can monitor their prices over time

## Acceptance Criteria

- [ ] Item has name, description, category, priority, URL
- [ ] Items belong to a user
- [ ] Items can have optional target price
- [ ] Items can be marked as favorite
- [ ] User can only manage their own items

---

## Task 2.2.1: Create ItemPriority Enum

```java
package com.yourname.personalfinance.entity.enums;

/**
 * Priority levels for items.
 */
public enum ItemPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

---

## Task 2.2.2: Create Item Entity

```java
package com.yourname.personalfinance.entity;

import com.yourname.personalfinance.entity.enums.ItemPriority;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Item entity representing something user wants to track.
 */
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_user", columnList = "user_id"),
    @Index(name = "idx_item_category", columnList = "category_id"),
    @Index(name = "idx_item_user_name", columnList = "user_id, name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;  // Optional
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 500)
    private String url;  // Product URL
    
    @Column(length = 100)
    private String brand;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ItemPriority priority = ItemPriority.MEDIUM;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal targetPrice;  // Desired price
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentPrice;  // Latest known price
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean favorite = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    
    @Column(length = 500)
    private String notes;
}
```

---

## Task 2.2.3: Create Item DTOs

```java
package com.yourname.personalfinance.dto.request;

import com.yourname.personalfinance.entity.enums.ItemPriority;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import java.math.BigDecimal;

public record CreateItemRequest(
    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    String name,
    
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description,
    
    Long categoryId,  // Optional
    
    @URL(message = "Must be a valid URL")
    @Size(max = 500, message = "URL must be at most 500 characters")
    String url,
    
    @Size(max = 100, message = "Brand must be at most 100 characters")
    String brand,
    
    ItemPriority priority,  // Defaults to MEDIUM if null
    
    @DecimalMin(value = "0.01", message = "Target price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    BigDecimal targetPrice,
    
    Boolean favorite,
    
    @Size(max = 500, message = "Notes must be at most 500 characters")
    String notes
) {}
```

```java
package com.yourname.personalfinance.dto.request;

import com.yourname.personalfinance.entity.enums.ItemPriority;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import java.math.BigDecimal;

public record UpdateItemRequest(
    @Size(max = 200, message = "Name must be at most 200 characters")
    String name,
    
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description,
    
    Long categoryId,
    
    @URL(message = "Must be a valid URL")
    @Size(max = 500, message = "URL must be at most 500 characters")
    String url,
    
    @Size(max = 100, message = "Brand must be at most 100 characters")
    String brand,
    
    ItemPriority priority,
    
    @DecimalMin(value = "0.01", message = "Target price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    BigDecimal targetPrice,
    
    Boolean favorite,
    
    Boolean archived,
    
    @Size(max = 500, message = "Notes must be at most 500 characters")
    String notes
) {}
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.Item;
import com.yourname.personalfinance.entity.enums.ItemPriority;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
    Long id,
    String name,
    String description,
    Long categoryId,
    String categoryName,
    String url,
    String brand,
    ItemPriority priority,
    BigDecimal targetPrice,
    BigDecimal currentPrice,
    Boolean favorite,
    Boolean archived,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getCategory() != null ? item.getCategory().getId() : null,
            item.getCategory() != null ? item.getCategory().getName() : null,
            item.getUrl(),
            item.getBrand(),
            item.getPriority(),
            item.getTargetPrice(),
            item.getCurrentPrice(),
            item.getFavorite(),
            item.getArchived(),
            item.getNotes(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
```

---

## Task 2.2.4: Create ItemRepository

```java
package com.yourname.personalfinance.repository;

import com.yourname.personalfinance.entity.Item;
import com.yourname.personalfinance.entity.enums.ItemPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    /**
     * Finds item by ID and user ID (security check).
     */
    Optional<Item> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Finds all items for user with pagination.
     */
    Page<Item> findByUserIdAndArchivedFalse(Long userId, Pageable pageable);
    
    /**
     * Finds items by category.
     */
    Page<Item> findByUserIdAndCategoryIdAndArchivedFalse(
        Long userId, Long categoryId, Pageable pageable);
    
    /**
     * Finds favorite items.
     */
    List<Item> findByUserIdAndFavoriteTrueAndArchivedFalse(Long userId);
    
    /**
     * Finds items by priority.
     */
    List<Item> findByUserIdAndPriorityAndArchivedFalse(Long userId, ItemPriority priority);
    
    /**
     * Checks if item name exists for user.
     */
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    
    /**
     * Finds item with category eagerly loaded.
     */
    @Query("SELECT i FROM Item i " +
           "LEFT JOIN FETCH i.category " +
           "WHERE i.id = :id AND i.user.id = :userId")
    Optional<Item> findByIdAndUserIdWithCategory(
        @Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * Finds all items for user with categories (prevents N+1).
     */
    @Query("SELECT i FROM Item i " +
           "LEFT JOIN FETCH i.category " +
           "WHERE i.user.id = :userId AND i.archived = false " +
           "ORDER BY i.name")
    List<Item> findAllByUserIdWithCategory(@Param("userId") Long userId);
    
    /**
     * Counts items by category.
     */
    long countByCategoryIdAndArchivedFalse(Long categoryId);
}
```

---

## Task 2.2.5: Create ItemService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.CreateItemRequest;
import com.yourname.personalfinance.dto.request.UpdateItemRequest;
import com.yourname.personalfinance.dto.response.ItemResponse;
import com.yourname.personalfinance.entity.Category;
import com.yourname.personalfinance.entity.Item;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.entity.enums.ItemPriority;
import com.yourname.personalfinance.exception.*;
import com.yourname.personalfinance.repository.CategoryRepository;
import com.yourname.personalfinance.repository.ItemRepository;
import com.yourname.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Creates a new item.
     */
    @Transactional
    public ItemResponse createItem(Long userId, CreateItemRequest request) {
        log.info("Creating item '{}' for user {}", request.name(), userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        // Validate category if provided
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found or not owned by user: " + request.categoryId()
                ));
        }
        
        Item item = Item.builder()
            .user(user)
            .category(category)
            .name(request.name())
            .description(request.description())
            .url(request.url())
            .brand(request.brand())
            .priority(request.priority() != null ? request.priority() : ItemPriority.MEDIUM)
            .targetPrice(request.targetPrice())
            .favorite(request.favorite() != null ? request.favorite() : false)
            .notes(request.notes())
            .build();
        
        Item saved = itemRepository.save(item);
        log.info("Item created with id: {}", saved.getId());
        
        return ItemResponse.from(saved);
    }
    
    /**
     * Gets item by ID.
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findByIdAndUserIdWithCategory(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        return ItemResponse.from(item);
    }
    
    /**
     * Gets all items for user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Long userId, Pageable pageable) {
        return itemRepository.findByUserIdAndArchivedFalse(userId, pageable)
            .map(ItemResponse::from);
    }
    
    /**
     * Gets items by category.
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsByCategory(Long userId, Long categoryId, Pageable pageable) {
        // Verify category ownership
        categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        
        return itemRepository.findByUserIdAndCategoryIdAndArchivedFalse(userId, categoryId, pageable)
            .map(ItemResponse::from);
    }
    
    /**
     * Gets favorite items.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getFavoriteItems(Long userId) {
        return itemRepository.findByUserIdAndFavoriteTrueAndArchivedFalse(userId)
            .stream()
            .map(ItemResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * Updates an item.
     */
    @Transactional
    public ItemResponse updateItem(Long itemId, Long userId, UpdateItemRequest request) {
        log.info("Updating item {} for user {}", itemId, userId);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        // Update category if provided
        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found: " + request.categoryId()
                ));
            item.setCategory(category);
        }
        
        // Update other fields
        if (request.name() != null) item.setName(request.name());
        if (request.description() != null) item.setDescription(request.description());
        if (request.url() != null) item.setUrl(request.url());
        if (request.brand() != null) item.setBrand(request.brand());
        if (request.priority() != null) item.setPriority(request.priority());
        if (request.targetPrice() != null) item.setTargetPrice(request.targetPrice());
        if (request.favorite() != null) item.setFavorite(request.favorite());
        if (request.archived() != null) item.setArchived(request.archived());
        if (request.notes() != null) item.setNotes(request.notes());
        
        return ItemResponse.from(item);
    }
    
    /**
     * Deletes an item.
     */
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting item {} for user {}", itemId, userId);
        
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        itemRepository.delete(item);
        log.info("Item {} deleted", itemId);
    }
    
    /**
     * Toggles favorite status.
     */
    @Transactional
    public ItemResponse toggleFavorite(Long itemId, Long userId) {
        Item item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        
        item.setFavorite(!item.getFavorite());
        return ItemResponse.from(item);
    }
}
```

---

## Task 2.2.6: Create ItemController

```java
package com.yourname.personalfinance.controller;

import com.yourname.personalfinance.dto.request.CreateItemRequest;
import com.yourname.personalfinance.dto.request.UpdateItemRequest;
import com.yourname.personalfinance.dto.response.ItemResponse;
import com.yourname.personalfinance.security.UserDetailsImpl;
import com.yourname.personalfinance.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    
    private final ItemService itemService;
    
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateItemRequest request) {
        
        Long userId = userDetails.getUser().getId();
        ItemResponse response = itemService.createItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        ItemResponse response = itemService.getItemById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getAllItems(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        Long userId = userDetails.getUser().getId();
        Page<ItemResponse> items = itemService.getAllItems(userId, pageable);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ItemResponse>> getItemsByCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Long userId = userDetails.getUser().getId();
        Page<ItemResponse> items = itemService.getItemsByCategory(userId, categoryId, pageable);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<List<ItemResponse>> getFavorites(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getUser().getId();
        List<ItemResponse> items = itemService.getFavoriteItems(userId);
        return ResponseEntity.ok(items);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        
        Long userId = userDetails.getUser().getId();
        ItemResponse response = itemService.updateItem(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        itemService.deleteItem(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/toggle-favorite")
    public ResponseEntity<ItemResponse> toggleFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Long userId = userDetails.getUser().getId();
        ItemResponse response = itemService.toggleFavorite(id, userId);
        return ResponseEntity.ok(response);
    }
}
```

---

## Story 2.2 Checklist

- [ ] ItemPriority enum created
- [ ] Item entity created
- [ ] Item DTOs created
- [ ] ItemRepository with query methods
- [ ] ItemService with CRUD operations
- [ ] ItemController with REST endpoints
- [ ] Category ownership validation
- [ ] Pagination support
- [ ] Toggle favorite endpoint
- [ ] Unit tests for ItemService

---

# Story 2.3: Item Search & Filtering

## User Story

**As a** user  
**I want to** search and filter my items  
**So that** I can quickly find what I'm looking for

## Acceptance Criteria

- [ ] Search by name (case-insensitive, partial match)
- [ ] Filter by category
- [ ] Filter by priority
- [ ] Filter by favorite status
- [ ] Results paginated
- [ ] Results sorted by name, priority, or creation date

---

## Task 2.3.1: Create Search Criteria DTO

```java
package com.yourname.personalfinance.dto.request;

import com.yourname.personalfinance.entity.enums.ItemPriority;

/**
 * Search criteria for filtering items.
 */
public record ItemSearchCriteria(
    String searchTerm,      // Name search (partial, case-insensitive)
    Long categoryId,        // Filter by category
    ItemPriority priority,  // Filter by priority
    Boolean favorite,       // Filter favorites only
    Boolean archived        // Include archived items
) {
    /**
     * Creates criteria with defaults.
     */
    public static ItemSearchCriteria defaults() {
        return new ItemSearchCriteria(null, null, null, null, false);
    }
}
```

---

## Task 2.3.2: Add Search Methods to Repository

```java
// Add to ItemRepository.java

/**
 * Searches items by name (case-insensitive partial match).
 */
@Query("SELECT i FROM Item i " +
       "LEFT JOIN FETCH i.category " +
       "WHERE i.user.id = :userId " +
       "AND i.archived = false " +
       "AND LOWER(i.name) LIKE LOWER(CONCAT('%', :term, '%'))")
List<Item> searchByName(@Param("userId") Long userId, @Param("term") String term);

/**
 * Advanced search with multiple criteria.
 */
@Query("SELECT i FROM Item i " +
       "LEFT JOIN FETCH i.category " +
       "WHERE i.user.id = :userId " +
       "AND (:archived IS NULL OR i.archived = :archived) " +
       "AND (:term IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :term, '%'))) " +
       "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
       "AND (:priority IS NULL OR i.priority = :priority) " +
       "AND (:favorite IS NULL OR i.favorite = :favorite)")
Page<Item> searchWithCriteria(
    @Param("userId") Long userId,
    @Param("term") String term,
    @Param("categoryId") Long categoryId,
    @Param("priority") ItemPriority priority,
    @Param("favorite") Boolean favorite,
    @Param("archived") Boolean archived,
    Pageable pageable
);
```

---

## Task 2.3.3: Add Search Method to Service

```java
// Add to ItemService.java

/**
 * Searches items with multiple criteria.
 */
@Transactional(readOnly = true)
public Page<ItemResponse> searchItems(Long userId, ItemSearchCriteria criteria, Pageable pageable) {
    log.debug("Searching items for user {} with criteria: {}", userId, criteria);
    
    return itemRepository.searchWithCriteria(
        userId,
        criteria.searchTerm(),
        criteria.categoryId(),
        criteria.priority(),
        criteria.favorite(),
        criteria.archived() != null ? criteria.archived() : false,
        pageable
    ).map(ItemResponse::from);
}

/**
 * Quick search by name only.
 */
@Transactional(readOnly = true)
public List<ItemResponse> quickSearch(Long userId, String term) {
    if (term == null || term.trim().isEmpty()) {
        return List.of();
    }
    
    return itemRepository.searchByName(userId, term.trim())
        .stream()
        .map(ItemResponse::from)
        .collect(Collectors.toList());
}
```

---

## Task 2.3.4: Add Search Endpoint to Controller

```java
// Add to ItemController.java

/**
 * Searches items with multiple criteria.
 * GET /api/items/search?term=apple&categoryId=1&priority=HIGH&favorite=true
 */
@GetMapping("/search")
public ResponseEntity<Page<ItemResponse>> searchItems(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false) String term,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) ItemPriority priority,
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false, defaultValue = "false") Boolean archived,
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    
    Long userId = userDetails.getUser().getId();
    log.info("GET /api/items/search - user: {}, term: {}", userId, term);
    
    ItemSearchCriteria criteria = new ItemSearchCriteria(
        term, categoryId, priority, favorite, archived
    );
    
    Page<ItemResponse> items = itemService.searchItems(userId, criteria, pageable);
    return ResponseEntity.ok(items);
}

/**
 * Quick search for autocomplete.
 * GET /api/items/quick-search?term=app
 */
@GetMapping("/quick-search")
public ResponseEntity<List<ItemResponse>> quickSearch(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam String term) {
    
    Long userId = userDetails.getUser().getId();
    List<ItemResponse> items = itemService.quickSearch(userId, term);
    return ResponseEntity.ok(items);
}
```

---

## Story 2.3 Checklist

- [ ] ItemSearchCriteria DTO created
- [ ] Search repository methods added
- [ ] searchItems service method implemented
- [ ] quickSearch service method implemented
- [ ] Search endpoints added to controller
- [ ] Pagination and sorting work correctly
- [ ] Unit tests for search methods
- [ ] Test all filter combinations

---

## Epic 2 Complete Checklist

Before moving to Epic 3:

- [ ] **Story 2.1:** Categories CRUD working
- [ ] **Story 2.2:** Items CRUD working with categories
- [ ] **Story 2.3:** Search and filtering working
- [ ] All ownership validations in place
- [ ] Pagination working on list endpoints
- [ ] Unit tests passing
- [ ] Integration tests passing

---

## Next Steps

→ Continue to [04-Epic3-Shopping-Lists.md](./04-Epic3-Shopping-Lists.md)
