# 📚 Patterns & Best Practices Reference

> **Type:** Reference Guide | **Use:** Quick lookup during development  
> **Purpose:** Reusable patterns, code snippets, and best practices

---

## 📋 Table of Contents

1. [Entity Patterns](#entity-patterns)
2. [DTO Patterns](#dto-patterns)
3. [Repository Patterns](#repository-patterns)
4. [Service Patterns](#service-patterns)
5. [Controller Patterns](#controller-patterns)
6. [Testing Patterns](#testing-patterns)
7. [Security Patterns](#security-patterns)
8. [Common Mistakes to Avoid](#common-mistakes-to-avoid)

---

## Entity Patterns

### Base Entity Template

```java
/**
 * Base entity providing common audit fields.
 * All entities should extend this class.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### Entity with User Ownership

```java
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    // Owner relationship - required
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Helper method for ownership checks
    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }
}
```

### One-to-Many Relationship

```java
// Parent side (User)
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Item> items = new ArrayList<>();  // Always initialize!

// Child side (Item)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

### Many-to-Many with Extra Columns

```java
// Instead of @ManyToMany, create a join entity

@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    // Extra columns
    private Integer quantity;
    private String unit;
    private LocalDateTime purchasedAt;
}
```

### Enum Mapping

```java
// Always use STRING for enums (survives enum reordering)
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private UserRole role = UserRole.USER;

// Define enum
public enum UserRole {
    USER,
    ADMIN
}
```

---

## DTO Patterns

### Request DTO (Record)

```java
/**
 * Request DTOs should:
 * - Use Java Records for immutability
 * - Include validation annotations
 * - Have clear error messages
 */
public record CreateItemRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be 1-200 characters")
    String name,
    
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    String description,
    
    @URL(message = "URL must be valid")
    String url,
    
    Long categoryId  // Optional - no validation
) {}
```

### Response DTO (Record with Factory)

```java
/**
 * Response DTOs should:
 * - Use Java Records
 * - Have a static factory method from entity
 * - Never expose passwords or sensitive data
 */
public record UserResponse(
    Long id,
    String email,
    String username,
    String firstName,
    String lastName,
    String fullName,
    UserRole role,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getFullName(),
            user.getUserRole(),
            user.getCreatedAt()
        );
    }
}
```

### Update DTO with Partial Updates

```java
public record UpdateItemRequest(
    @Size(min = 1, max = 200, message = "Name must be 1-200 characters")
    String name,  // Optional - only updates if not null
    
    String description,
    
    String url,
    
    Long categoryId
) {}

// In service - only update non-null fields
public void updateItem(Item item, UpdateItemRequest request) {
    if (request.name() != null) {
        item.setName(request.name());
    }
    if (request.description() != null) {
        item.setDescription(request.description());
    }
    // ... etc
}
```

---

## Repository Patterns

### Basic Repository

```java
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Derived query methods
    List<Item> findByUserId(Long userId);
    Optional<Item> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    long countByUserId(Long userId);
}
```

### Custom Queries with @Query

```java
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // JPQL query
    @Query("""
        SELECT i FROM Item i
        WHERE i.user.id = :userId
        AND LOWER(i.name) LIKE LOWER(CONCAT('%', :term, '%'))
        """)
    List<Item> searchByName(@Param("userId") Long userId, @Param("term") String term);
    
    // JOIN FETCH to prevent N+1
    @Query("""
        SELECT i FROM Item i
        LEFT JOIN FETCH i.category
        WHERE i.user.id = :userId
        """)
    List<Item> findByUserIdWithCategory(@Param("userId") Long userId);
    
    // Aggregation
    @Query("SELECT AVG(pe.price) FROM PriceEntry pe WHERE pe.item.id = :itemId")
    BigDecimal findAveragePrice(@Param("itemId") Long itemId);
}
```

### Pagination

```java
// Repository method
Page<Item> findByUserId(Long userId, Pageable pageable);

// Service usage
public Page<ItemResponse> getItems(Long userId, int page, int size, String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
    return itemRepository.findByUserId(userId, pageable)
        .map(ItemResponse::from);
}

// Controller usage
@GetMapping
public ResponseEntity<Page<ItemResponse>> getItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sortBy) {
    // ...
}
```

---

## Service Patterns

### Service Class Structure

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    // ========== READ OPERATIONS ==========
    
    @Transactional(readOnly = true)  // Optimize for reads
    public ItemResponse getItemById(Long itemId, Long userId) {
        log.debug("Fetching item {} for user {}", itemId, userId);
        
        return itemRepository.findByIdAndUserId(itemId, userId)
            .map(ItemResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Item", itemId));
    }
    
    // ========== WRITE OPERATIONS ==========
    
    @Transactional  // Default - read-write
    public ItemResponse createItem(CreateItemRequest request, Long userId) {
        log.info("Creating item '{}' for user {}", request.name(), userId);
        
        // Validation
        validateUniqueItemName(userId, request.name());
        
        // Build entity
        Item item = new Item();
        item.setName(request.name());
        // ... set other fields
        
        // Save
        Item saved = itemRepository.save(item);
        log.info("Created item with ID: {}", saved.getId());
        
        return ItemResponse.from(saved);
    }
}
```

### Validation in Service

```java
private void validateUniqueItemName(Long userId, String name) {
    if (itemRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
        throw new DuplicateResourceException(
            "Item with name '" + name + "' already exists"
        );
    }
}

private void validateOwnership(Item item, Long userId) {
    if (!item.isOwnedBy(userId)) {
        throw new AccessDeniedException("You don't own this item");
    }
}

private Category validateAndGetCategory(Long categoryId, Long userId) {
    if (categoryId == null) {
        return null;
    }
    return categoryRepository.findByIdAndUserId(categoryId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
}
```

### Service Composition

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserService userService;      // Inject other services
    private final ItemService itemService;
    private final NotificationService notificationService;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        // Use other services
        UserResponse user = userService.getUserById(userId);
        ItemResponse item = itemService.getItemById(request.itemId(), userId);
        
        // Create order
        Order order = new Order();
        // ...
        
        Order saved = orderRepository.save(order);
        
        // Trigger side effects
        notificationService.sendOrderConfirmation(saved);
        
        return OrderResponse.from(saved);
    }
}
```

---

## Controller Patterns

### RESTful Controller Structure

```java
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Items")
public class ItemController {
    
    private final ItemService itemService;
    
    // GET - single resource
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ItemResponse item = itemService.getItemById(id, userDetails.getUserId());
        return ResponseEntity.ok(item);
    }
    
    // GET - collection (paginated)
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Page<ItemResponse> items = itemService.getItemsByUser(
            userDetails.getUserId(), page, size
        );
        return ResponseEntity.ok(items);
    }
    
    // POST - create
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ItemResponse item = itemService.createItem(request, userDetails.getUserId());
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(item.id())
            .toUri();
        
        return ResponseEntity.created(location).body(item);
    }
    
    // PUT - update
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ItemResponse item = itemService.updateItem(id, request, userDetails.getUserId());
        return ResponseEntity.ok(item);
    }
    
    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        itemService.deleteItem(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
```

### Getting Current User

```java
// Option 1: @AuthenticationPrincipal
@GetMapping("/me")
public ResponseEntity<UserResponse> getCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    Long userId = userDetails.getUser().getId();
    // ...
}

// Option 2: SecurityContextHolder (for services)
public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }
    throw new IllegalStateException("No authenticated user");
}
```

---

## Testing Patterns

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService Unit Tests")
class ItemServiceTest {
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ItemService itemService;
    
    private User testUser;
    private Item testItem;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setUser(testUser);
    }
    
    @Nested
    @DisplayName("getItemById")
    class GetItemById {
        
        @Test
        @DisplayName("when item exists - should return item")
        void whenExists_ReturnsItem() {
            // Arrange
            when(itemRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testItem));
            
            // Act
            ItemResponse response = itemService.getItemById(1L, 1L);
            
            // Assert
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Test Item");
            verify(itemRepository).findByIdAndUserId(1L, 1L);
        }
        
        @Test
        @DisplayName("when item not found - should throw exception")
        void whenNotFound_ThrowsException() {
            // Arrange
            when(itemRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> itemService.getItemById(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
```

### Integration Test Template

```java
@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security for unit tests
class ItemControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ItemService itemService;
    
    @Test
    @DisplayName("GET /api/items/{id} - should return item")
    void getItem_ReturnsItem() throws Exception {
        // Arrange
        ItemResponse response = new ItemResponse(1L, "Test Item", null, null);
        when(itemService.getItemById(eq(1L), any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Item"));
    }
    
    @Test
    @DisplayName("POST /api/items - should create item")
    void createItem_ReturnsCreated() throws Exception {
        // Arrange
        CreateItemRequest request = new CreateItemRequest("New Item", null, null, null);
        ItemResponse response = new ItemResponse(1L, "New Item", null, null);
        when(itemService.createItem(any(), any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }
}
```

### Repository Test Template

```java
@DataJpaTest
class ItemRepositoryTest {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        // ... set required fields
        entityManager.persist(testUser);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("findByIdAndUserId - should return item when owned")
    void findByIdAndUserId_WhenOwned_ReturnsItem() {
        // Arrange
        Item item = new Item();
        item.setName("Test");
        item.setUser(testUser);
        entityManager.persist(item);
        entityManager.flush();
        
        // Act
        Optional<Item> found = itemRepository.findByIdAndUserId(
            item.getId(), testUser.getId()
        );
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }
}
```

---

## Security Patterns

### Get Current User ID

```java
// In Controller - use @AuthenticationPrincipal
@GetMapping("/me")
public UserResponse getCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.getUserById(userDetails.getUser().getId());
}

// In Service - use SecurityContext
public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetailsImpl details) {
        return details.getUser().getId();
    }
    throw new IllegalStateException("User not authenticated");
}
```

### Ownership Validation

```java
@Service
public class ItemService {
    
    public ItemResponse getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Item", itemId));
        
        // Check ownership
        if (!item.isOwnedBy(userId)) {
            throw new AccessDeniedException("You don't own this item");
        }
        
        return ItemResponse.from(item);
    }
    
    // Or use repository method that includes userId
    public ItemResponse getItemSecure(Long itemId, Long userId) {
        return itemRepository.findByIdAndUserId(itemId, userId)
            .map(ItemResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Item", itemId));
    }
}
```

### Method-Level Security

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<UserResponse> getAllUsers() {
    return userService.getAllUsers();
}

@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
@GetMapping("/users/{userId}")
public UserResponse getUser(@PathVariable Long userId) {
    return userService.getUserById(userId);
}
```

---

## Common Mistakes to Avoid

### ❌ DON'T: Return Entity from Controller

```java
// BAD - exposes internal structure, password, etc.
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}

// GOOD - use DTO
@GetMapping("/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userService.getUserById(id);  // Returns UserResponse
}
```

### ❌ DON'T: Business Logic in Controller

```java
// BAD - controller doing business logic
@PostMapping
public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
        return ResponseEntity.badRequest().body("Email exists");
    }
    User user = new User();
    user.setPassword(passwordEncoder.encode(request.password()));
    // ... more logic
}

// GOOD - delegate to service
@PostMapping
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    UserResponse response = userService.createUser(request);
    return ResponseEntity.created(location).body(response);
}
```

### ❌ DON'T: Forget Transaction Annotations

```java
// BAD - no transaction, may cause issues
public void transferMoney(Long from, Long to, BigDecimal amount) {
    accountRepository.debit(from, amount);
    // If this fails, debit already happened!
    accountRepository.credit(to, amount);
}

// GOOD - atomic transaction
@Transactional
public void transferMoney(Long from, Long to, BigDecimal amount) {
    accountRepository.debit(from, amount);
    accountRepository.credit(to, amount);
}
```

### ❌ DON'T: N+1 Query Problem

```java
// BAD - N+1 queries (1 for users, N for items)
List<User> users = userRepository.findAll();
for (User user : users) {
    List<Item> items = user.getItems();  // Lazy load - new query each time!
}

// GOOD - JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.items")
List<User> findAllWithItems();
```

### ❌ DON'T: Trust Client Input for User ID

```java
// BAD - client can fake userId
@PostMapping("/items")
public ItemResponse createItem(
        @RequestBody CreateItemRequest request,
        @RequestParam Long userId) {  // Client-provided!
    return itemService.createItem(request, userId);
}

// GOOD - get userId from authenticated user
@PostMapping("/items")
public ItemResponse createItem(
        @RequestBody CreateItemRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return itemService.createItem(request, userDetails.getUser().getId());
}
```

### ❌ DON'T: Expose Internal Errors

```java
// BAD - exposes stack trace
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handle(Exception e) {
    return ResponseEntity.status(500).body(e.getMessage());
}

// GOOD - generic message, log details
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handle(Exception e) {
    log.error("Unexpected error", e);  // Log for debugging
    return ResponseEntity.status(500).body(
        ErrorResponse.of(500, "INTERNAL_ERROR", 
            "An unexpected error occurred", "/path")
    );
}
```

### ❌ DON'T: Use `ddl-auto: create` in Production

```yaml
# BAD - will drop all data!
spring:
  jpa:
    hibernate:
      ddl-auto: create

# GOOD - let Flyway manage schema
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

---

## Quick Reference Cards

### HTTP Status Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| 200 | OK | GET, PUT success |
| 201 | Created | POST success |
| 204 | No Content | DELETE success |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | Not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Server Error | Unexpected error |

### Validation Annotations

| Annotation | Purpose |
|------------|---------|
| @NotNull | Field must not be null |
| @NotBlank | String must have content |
| @NotEmpty | Collection must have elements |
| @Size(min, max) | String/collection length |
| @Min, @Max | Numeric bounds |
| @Email | Valid email format |
| @URL | Valid URL format |
| @Pattern | Regex match |
| @Positive | Number > 0 |
| @Future, @Past | Date validation |

### Lombok Annotations

| Annotation | Generates |
|------------|-----------|
| @Getter | All getters |
| @Setter | All setters |
| @NoArgsConstructor | Default constructor |
| @AllArgsConstructor | All-args constructor |
| @RequiredArgsConstructor | Constructor for final fields |
| @Builder | Builder pattern |
| @Data | Getters, setters, equals, hashCode, toString |
| @Slf4j | Logger field |

---

**Use this guide as a quick reference while developing!** 🚀
