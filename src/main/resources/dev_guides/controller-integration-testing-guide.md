# Controller & Integration Testing Guide for Spring Boot

> **Your Reference Guide**: A comprehensive, practical guide to testing Spring MVC controllers and writing integration tests that verify your application's behavior across multiple layers.

---

## Table of Contents

1. [Controller vs Integration Testing](#controller-vs-integration-testing)
2. [Essential Testing Tools](#essential-testing-tools)
3. [Part 1: Controller Testing (@WebMvcTest)](#part-1-controller-testing-webmvctest)
4. [Part 2: Integration Testing (@SpringBootTest)](#part-2-integration-testing-springboottest)
5. [Testing Security](#testing-security)
6. [Testing Validation & Error Handling](#testing-validation--error-handling)
7. [Complete Examples](#complete-examples)
8. [Testing Best Practices](#testing-best-practices)
9. [Common Patterns & Solutions](#common-patterns--solutions)
10. [Anti-Patterns to Avoid](#anti-patterns-to-avoid)

---

## Controller vs Integration Testing

### What's the Difference?

| Aspect | Controller Test | Integration Test |
|--------|----------------|------------------|
| **Scope** | Web layer only | Multiple layers (Controller → Service → Repository → DB) |
| **Annotation** | `@WebMvcTest` | `@SpringBootTest` |
| **Dependencies** | Mocked | Real (or TestContainers) |
| **Database** | No | Yes (in-memory or TestContainers) |
| **Speed** | Fast (~100ms) | Slower (~2-5s) |
| **Purpose** | Test request/response handling | Test full application flow |
| **When to Use** | Verify controller logic, validation, mapping | Verify end-to-end behavior, data persistence |

### The Testing Strategy

```
Controller Tests (Many)     →  Test each endpoint's request/response handling
        ↓
Integration Tests (Some)    →  Test critical flows end-to-end
        ↓
E2E Tests (Few)            →  Test complete user journeys
```

**Rule of thumb:**
- ✅ Controller tests for **all** endpoints
- ✅ Integration tests for **critical business flows**
- ✅ E2E tests for **most important user journeys**

---

## Essential Testing Tools

### Dependencies Already in spring-boot-starter-test

```xml
<!-- This includes everything you need -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**What's included:**
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Hamcrest (for MockMvc result matchers)
- Spring Test & Spring Boot Test
- JSONassert (for JSON comparisons)
- JsonPath (for JSON querying)

### Optional: For Database Integration Tests

```xml
<!-- In-memory database for testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Or use TestContainers for real databases -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Part 1: Controller Testing (@WebMvcTest)

### What Are We Testing?

Controller tests verify the **web layer** in isolation:
- ✅ Request mapping (HTTP method, path, parameters)
- ✅ Request validation (Bean Validation)
- ✅ Response status codes (200, 400, 404, etc.)
- ✅ Response body structure (JSON format)
- ✅ Exception handling (GlobalExceptionHandler)
- ✅ Security (authentication/authorization)

**We are NOT testing:**
- ❌ Business logic (that's in service layer tests)
- ❌ Database operations (that's in integration tests)
- ❌ Transaction management

---

### Setting Up Controller Tests

#### Basic Structure

```java
package com.finance.controller;

import com.finance.dto.request.ItemCreateRequest;
import com.finance.dto.response.ItemResponse;
import com.finance.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Web layer tests for ItemController.
 * 
 * What @WebMvcTest does:
 * - Loads only the web layer (controllers, filters, @ControllerAdvice)
 * - Auto-configures MockMvc
 * - Disables full auto-configuration
 * - Does NOT load @Service, @Repository, or @Component beans
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests without starting a server

    @Autowired
    private ObjectMapper objectMapper; // For JSON serialization/deserialization

    @MockBean
    private ItemService itemService; // Mock the service layer

    // Tests go here...
}
```

#### What Each Component Does

**`@WebMvcTest(ItemController.class)`**
- Loads ONLY the specified controller
- Configures MockMvc automatically
- Scans for `@ControllerAdvice`, filters, converters
- Does NOT load the full Spring context

**`MockMvc`**
- Simulates HTTP requests without starting a real server
- Allows you to test the full Spring MVC flow
- Faster than real HTTP tests

**`ObjectMapper`**
- Converts Java objects to/from JSON
- Same instance Spring Boot uses for REST APIs
- Configured with your application's Jackson settings

**`@MockBean`**
- Creates a mock of the service
- Registers it in the Spring context
- Replaces any existing bean of the same type

---

### Testing HTTP Methods

#### GET Requests

```java
@Test
void getAllItems_returnsItemList() throws Exception {
    // Arrange
    List<ItemResponse> items = List.of(
        new ItemResponse(1L, "Laptop", "Electronics", "Gaming laptop"),
        new ItemResponse(2L, "Mouse", "Electronics", "Wireless mouse")
    );
    
    when(itemService.getAllItems()).thenReturn(items);
    
    // Act & Assert
    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Laptop"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name").value("Mouse"));
}

@Test
void getItemById_existingId_returnsItem() throws Exception {
    // Arrange
    ItemResponse item = new ItemResponse(1L, "Laptop", "Electronics", "Gaming laptop");
    when(itemService.getItemById(1L)).thenReturn(item);
    
    // Act & Assert
    mockMvc.perform(get("/api/items/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Laptop"))
        .andExpect(jsonPath("$.category").value("Electronics"));
}

@Test
void getItemById_nonExistingId_returnsNotFound() throws Exception {
    // Arrange
    when(itemService.getItemById(999L))
        .thenThrow(new ResourceNotFoundException("Item not found with id: 999"));
    
    // Act & Assert
    mockMvc.perform(get("/api/items/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Item not found with id: 999"));
}
```

#### POST Requests

```java
@Test
void createItem_validRequest_returnsCreated() throws Exception {
    // Arrange
    ItemCreateRequest request = new ItemCreateRequest(
        "Laptop",
        "Electronics",
        "Gaming laptop"
    );
    
    ItemResponse response = new ItemResponse(1L, "Laptop", "Electronics", "Gaming laptop");
    when(itemService.createItem(any(ItemCreateRequest.class))).thenReturn(response);
    
    // Act & Assert
    mockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Laptop"));
}

@Test
void createItem_invalidRequest_returnsBadRequest() throws Exception {
    // Arrange - Request with blank name (violates @NotBlank)
    ItemCreateRequest request = new ItemCreateRequest(
        "",  // Blank name
        "Electronics",
        "Description"
    );
    
    // Act & Assert
    mockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
}
```

#### PUT Requests

```java
@Test
void updateItem_validRequest_returnsOk() throws Exception {
    // Arrange
    ItemUpdateRequest request = new ItemUpdateRequest(
        "Updated Laptop",
        "Electronics",
        "High-end gaming laptop"
    );
    
    ItemResponse response = new ItemResponse(1L, "Updated Laptop", "Electronics", "High-end gaming laptop");
    when(itemService.updateItem(eq(1L), any(ItemUpdateRequest.class))).thenReturn(response);
    
    // Act & Assert
    mockMvc.perform(put("/api/items/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Laptop"));
}

@Test
void updateItem_nonExistingId_returnsNotFound() throws Exception {
    // Arrange
    ItemUpdateRequest request = new ItemUpdateRequest("Name", "Category", "Description");
    when(itemService.updateItem(eq(999L), any(ItemUpdateRequest.class)))
        .thenThrow(new ResourceNotFoundException("Item not found with id: 999"));
    
    // Act & Assert
    mockMvc.perform(put("/api/items/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
}
```

#### DELETE Requests

```java
@Test
void deleteItem_existingId_returnsNoContent() throws Exception {
    // Arrange - Service method is void, so no need to stub return value
    // Just ensure it doesn't throw an exception
    
    // Act & Assert
    mockMvc.perform(delete("/api/items/{id}", 1L))
        .andExpect(status().isNoContent());
    
    // Verify the service was called
    verify(itemService).deleteItem(1L);
}

@Test
void deleteItem_nonExistingId_returnsNotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Item not found with id: 999"))
        .when(itemService).deleteItem(999L);
    
    // Act & Assert
    mockMvc.perform(delete("/api/items/{id}", 999L))
        .andExpect(status().isNotFound());
}
```

---

### Testing Query Parameters

```java
@Test
void searchItems_withQueryParams_returnsFilteredResults() throws Exception {
    // Arrange
    List<ItemResponse> items = List.of(
        new ItemResponse(1L, "Gaming Laptop", "Electronics", "High-end laptop")
    );
    
    when(itemService.searchItems("laptop", "Electronics")).thenReturn(items);
    
    // Act & Assert
    mockMvc.perform(get("/api/items/search")
            .param("keyword", "laptop")
            .param("category", "Electronics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Gaming Laptop"));
}

@Test
void searchItems_withoutRequiredParam_returnsBadRequest() throws Exception {
    // Missing required parameter
    mockMvc.perform(get("/api/items/search"))
        .andExpect(status().isBadRequest());
}
```

---

### Testing Pagination

```java
@Test
void getAllItems_withPagination_returnsPaginatedResults() throws Exception {
    // Arrange
    List<ItemResponse> items = List.of(
        new ItemResponse(1L, "Item 1", "Category", "Description"),
        new ItemResponse(2L, "Item 2", "Category", "Description")
    );
    
    Page<ItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 2), 10);
    when(itemService.getAllItems(any(Pageable.class))).thenReturn(page);
    
    // Act & Assert
    mockMvc.perform(get("/api/items")
            .param("page", "0")
            .param("size", "2")
            .param("sort", "name,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements").value(10))
        .andExpect(jsonPath("$.totalPages").value(5))
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(2));
}
```

---

### JSONPath Cheat Sheet

JSONPath allows you to query JSON responses:

```java
// Basic access
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.name").value("Laptop"))

// Nested objects
.andExpect(jsonPath("$.user.email").value("john@example.com"))
.andExpect(jsonPath("$.address.city").value("New York"))

// Arrays
.andExpect(jsonPath("$", hasSize(3)))                  // Array has 3 elements
.andExpect(jsonPath("$[0].id").value(1))              // First element's id
.andExpect(jsonPath("$[*].name", hasItem("Laptop")))  // Any element has name "Laptop"

// Checking existence
.andExpect(jsonPath("$.id").exists())
.andExpect(jsonPath("$.deletedAt").doesNotExist())

// Matchers (from Hamcrest)
.andExpect(jsonPath("$.price").value(greaterThan(100)))
.andExpect(jsonPath("$.name").value(containsString("Laptop")))
.andExpect(jsonPath("$.status").value(isIn(List.of("ACTIVE", "PENDING"))))
```

---

## Part 2: Integration Testing (@SpringBootTest)

### What Are We Testing?

Integration tests verify **multiple layers working together**:
- ✅ Controller → Service → Repository → Database
- ✅ Transaction management
- ✅ Data persistence and retrieval
- ✅ Business logic execution
- ✅ Exception handling across layers
- ✅ Security end-to-end

**We ARE testing:**
- ✅ The complete request/response flow
- ✅ Database operations (with real or in-memory DB)
- ✅ Transaction rollback on errors
- ✅ Data validation at all layers

---

### Setting Up Integration Tests

#### Basic Structure with H2 (In-Memory)

```java
package com.finance.integration;

import com.finance.dto.request.ItemCreateRequest;
import com.finance.dto.response.ItemResponse;
import com.finance.entity.Item;
import com.finance.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Item management.
 * 
 * What @SpringBootTest does:
 * - Loads the FULL Spring application context
 * - Starts all beans (controllers, services, repositories)
 * - Uses the database configured in application-test.yml
 * - Slower than @WebMvcTest but tests real integration
 */
@SpringBootTest
@AutoConfigureMockMvc  // Configures MockMvc for integration tests
@ActiveProfiles("test") // Uses application-test.yml
@Transactional         // Rolls back changes after each test
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository; // Real repository

    @BeforeEach
    void setUp() {
        // Clean database before each test
        itemRepository.deleteAll();
    }

    // Tests go here...
}
```

#### Configuration: application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recreate schema for each test run
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  h2:
    console:
      enabled: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

### Full Flow Integration Tests

#### Create Item - Full Stack Test

```java
@Test
void createItem_validRequest_persistsToDatabase() throws Exception {
    // Arrange
    ItemCreateRequest request = new ItemCreateRequest(
        "Laptop",
        "Electronics",
        "Gaming laptop"
    );
    
    // Act - Make HTTP request
    String responseBody = mockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Laptop"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    // Parse response to get created ID
    ItemResponse response = objectMapper.readValue(responseBody, ItemResponse.class);
    
    // Assert - Verify database persistence
    Item savedItem = itemRepository.findById(response.id()).orElseThrow();
    assertThat(savedItem.getName()).isEqualTo("Laptop");
    assertThat(savedItem.getCategory()).isEqualTo("Electronics");
    assertThat(savedItem.getDescription()).isEqualTo("Gaming laptop");
    assertThat(savedItem.getCreatedAt()).isNotNull();
}
```

#### Read Item - Database Query Test

```java
@Test
void getItemById_existingItem_returnsFromDatabase() throws Exception {
    // Arrange - Insert directly into database
    Item item = Item.builder()
        .name("Laptop")
        .category("Electronics")
        .description("Gaming laptop")
        .build();
    Item savedItem = itemRepository.save(item);
    
    // Act & Assert - Verify HTTP response matches database
    mockMvc.perform(get("/api/items/{id}", savedItem.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedItem.getId()))
        .andExpect(jsonPath("$.name").value("Laptop"))
        .andExpect(jsonPath("$.category").value("Electronics"));
}
```

#### Update Item - Modification Test

```java
@Test
void updateItem_validRequest_updatesDatabase() throws Exception {
    // Arrange - Create initial item
    Item item = Item.builder()
        .name("Old Name")
        .category("Old Category")
        .description("Old Description")
        .build();
    Item savedItem = itemRepository.save(item);
    
    ItemUpdateRequest request = new ItemUpdateRequest(
        "New Name",
        "New Category",
        "New Description"
    );
    
    // Act - Update via HTTP
    mockMvc.perform(put("/api/items/{id}", savedItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"));
    
    // Assert - Verify database was updated
    Item updatedItem = itemRepository.findById(savedItem.getId()).orElseThrow();
    assertThat(updatedItem.getName()).isEqualTo("New Name");
    assertThat(updatedItem.getCategory()).isEqualTo("New Category");
    assertThat(updatedItem.getDescription()).isEqualTo("New Description");
    assertThat(updatedItem.getUpdatedAt()).isAfter(updatedItem.getCreatedAt());
}
```

#### Delete Item - Removal Test

```java
@Test
void deleteItem_existingItem_removesFromDatabase() throws Exception {
    // Arrange
    Item item = Item.builder()
        .name("Laptop")
        .category("Electronics")
        .build();
    Item savedItem = itemRepository.save(item);
    
    // Act - Delete via HTTP
    mockMvc.perform(delete("/api/items/{id}", savedItem.getId()))
        .andExpect(status().isNoContent());
    
    // Assert - Verify item is deleted from database
    assertThat(itemRepository.findById(savedItem.getId())).isEmpty();
}
```

---

### Testing Business Logic Flows

#### Complex Business Rule - Price Alert Creation

```java
@Test
void createPriceAlert_whenPriceDropsBelowThreshold_sendsNotification() throws Exception {
    // Arrange - Setup item with initial price
    Item item = itemRepository.save(
        Item.builder()
            .name("Laptop")
            .category("Electronics")
            .build()
    );
    
    Store store = storeRepository.save(
        Store.builder()
            .name("Best Buy")
            .location("New York")
            .build()
    );
    
    // Create initial price observation
    PriceObservation initialPrice = priceObservationRepository.save(
        PriceObservation.builder()
            .item(item)
            .store(store)
            .price(new BigDecimal("1000.00"))
            .observedAt(LocalDateTime.now().minusDays(1))
            .build()
    );
    
    // Create price alert
    PriceAlertRequest alertRequest = new PriceAlertRequest(
        item.getId(),
        store.getId(),
        new BigDecimal("900.00")  // Alert when price goes below $900
    );
    
    mockMvc.perform(post("/api/price-alerts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(alertRequest)))
        .andExpect(status().isCreated());
    
    // Act - Add new price below threshold
    PriceObservationRequest newPrice = new PriceObservationRequest(
        item.getId(),
        store.getId(),
        new BigDecimal("850.00")
    );
    
    mockMvc.perform(post("/api/price-observations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newPrice)))
        .andExpect(status().isCreated());
    
    // Assert - Verify alert was triggered (check notification table or event)
    List<PriceAlert> alerts = priceAlertRepository.findAll();
    assertThat(alerts).hasSize(1);
    assertThat(alerts.get(0).isTriggered()).isTrue();
    assertThat(alerts.get(0).getTriggeredAt()).isNotNull();
}
```

---

### Testing Relationships and Joins

```java
@Test
void getItemWithPriceHistory_returnsItemWithAllPrices() throws Exception {
    // Arrange - Create item with multiple price observations
    Item item = itemRepository.save(
        Item.builder()
            .name("Laptop")
            .category("Electronics")
            .build()
    );
    
    Store store1 = storeRepository.save(Store.builder().name("Best Buy").build());
    Store store2 = storeRepository.save(Store.builder().name("Amazon").build());
    
    priceObservationRepository.saveAll(List.of(
        PriceObservation.builder()
            .item(item)
            .store(store1)
            .price(new BigDecimal("1000.00"))
            .observedAt(LocalDateTime.now().minusDays(2))
            .build(),
        PriceObservation.builder()
            .item(item)
            .store(store1)
            .price(new BigDecimal("950.00"))
            .observedAt(LocalDateTime.now().minusDays(1))
            .build(),
        PriceObservation.builder()
            .item(item)
            .store(store2)
            .price(new BigDecimal("980.00"))
            .observedAt(LocalDateTime.now().minusDays(1))
            .build()
    ));
    
    // Act & Assert
    mockMvc.perform(get("/api/items/{id}/price-history", item.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.item.name").value("Laptop"))
        .andExpect(jsonPath("$.priceHistory", hasSize(3)))
        .andExpect(jsonPath("$.priceHistory[0].price").value(950.00))
        .andExpect(jsonPath("$.priceHistory[0].store.name").value("Best Buy"));
}
```

---

### Testing Transactions and Rollback

```java
@Test
void createOrder_withInsufficientStock_rollsBackTransaction() throws Exception {
    // Arrange - Create item with limited stock
    Item item = itemRepository.save(
        Item.builder()
            .name("Limited Edition")
            .stock(5)
            .build()
    );
    
    OrderRequest request = new OrderRequest(
        item.getId(),
        10  // Requesting more than available
    );
    
    // Act & Assert - Attempt to create order
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Insufficient stock")));
    
    // Assert - Verify stock wasn't changed (transaction rolled back)
    Item unchangedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertThat(unchangedItem.getStock()).isEqualTo(5);
    
    // Verify no order was created
    assertThat(orderRepository.findAll()).isEmpty();
}

@Test
void createOrder_successful_updatesStockAndCreatesOrder() throws Exception {
    // Arrange
    Item item = itemRepository.save(
        Item.builder()
            .name("Available Item")
            .stock(10)
            .price(new BigDecimal("100.00"))
            .build()
    );
    
    OrderRequest request = new OrderRequest(item.getId(), 3);
    
    // Act
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    
    // Assert - Both operations succeeded
    Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertThat(updatedItem.getStock()).isEqualTo(7);
    
    List<Order> orders = orderRepository.findAll();
    assertThat(orders).hasSize(1);
    assertThat(orders.get(0).getQuantity()).isEqualTo(3);
}
```

---

### Using TestContainers for Real Database

```java
package com.finance.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests using TestContainers with real MySQL.
 * 
 * Benefits:
 * - Tests against actual production database (MySQL)
 * - Catches database-specific issues
 * - Tests migrations and schema
 * 
 * Tradeoffs:
 * - Slower than H2 (container startup time)
 * - Requires Docker
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ItemIntegrationTestWithMySQL {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    // Same tests as before, but running against real MySQL
}
```

---

## Testing Security

### Testing JWT Authentication

#### Controller Test with Security

```java
package com.finance.controller;

import com.finance.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testing secured endpoints.
 * 
 * Import your SecurityConfig to test security rules.
 */
@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)  // Import security configuration
class SecuredItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtService jwtService;  // Mock JWT service if used

    @Test
    void getItems_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser  // Simulates authenticated user
    void getItems_withAuthentication_returnsOk() throws Exception {
        when(itemService.getAllItems()).thenReturn(List.of());
        
        mockMvc.perform(get("/api/items"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteItem_withUserRole_returnsForbidden() throws Exception {
        // User role can't delete
        mockMvc.perform(delete("/api/items/{id}", 1L))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItem_withAdminRole_returnsNoContent() throws Exception {
        // Admin role can delete
        mockMvc.perform(delete("/api/items/{id}", 1L))
            .andExpect(status().isNoContent());
    }
}
```

#### Integration Test with JWT Token

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecuredIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create test user and generate JWT
        User user = userRepository.save(
            User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build()
        );
        
        jwtToken = jwtService.generateToken(user);
    }

    @Test
    void getItems_withValidToken_returnsOk() throws Exception {
        mockMvc.perform(get("/api/items")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk());
    }

    @Test
    void getItems_withInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getItems_withExpiredToken_returnsUnauthorized() throws Exception {
        // Generate expired token (if your JwtService supports it for testing)
        String expiredToken = jwtService.generateExpiredToken(user);
        
        mockMvc.perform(get("/api/items")
                .header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## Testing Validation & Error Handling

### Testing Bean Validation

```java
@WebMvcTest(ItemController.class)
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_blankName_returnsBadRequest() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(
            "",  // Violates @NotBlank
            "Electronics",
            "Description"
        );
        
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    void createItem_nameTooLong_returnsBadRequest() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(
            "a".repeat(256),  // Violates @Size(max=255)
            "Electronics",
            "Description"
        );
        
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").value(containsString("must be between")));
    }

    @Test
    void createItem_invalidEmail_returnsBadRequest() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
            "username",
            "not-an-email",  // Violates @Email
            "password"
        );
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
    }

    @Test
    void createItem_multipleValidationErrors_returnsAllErrors() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(
            "",           // Blank name
            "",           // Blank category
            "a".repeat(1001)  // Description too long
        );
        
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.category").exists())
            .andExpect(jsonPath("$.errors.description").exists());
    }
}
```

---

### Testing Custom Validation

```java
@Test
void createPriceObservation_priceNegative_returnsBadRequest() throws Exception {
    // Assuming @PositivePrice custom validator
    PriceObservationRequest request = new PriceObservationRequest(
        1L,
        1L,
        new BigDecimal("-10.00")  // Negative price
    );
    
    mockMvc.perform(post("/api/price-observations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.price").value("Price must be positive"));
}
```

---

### Testing Global Exception Handler

```java
@Test
void getItem_nonExistingId_returnsStandardErrorFormat() throws Exception {
    when(itemService.getItemById(999L))
        .thenThrow(new ResourceNotFoundException("Item not found with id: 999"));
    
    mockMvc.perform(get("/api/items/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Item not found with id: 999"))
        .andExpect(jsonPath("$.path").value("/api/items/999"));
}

@Test
void createItem_duplicateName_returnsConflict() throws Exception {
    ItemCreateRequest request = new ItemCreateRequest("Duplicate", "Category", "Desc");
    
    when(itemService.createItem(any()))
        .thenThrow(new DuplicateResourceException("Item with name 'Duplicate' already exists"));
    
    mockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value(containsString("already exists")));
}
```

---

## Complete Examples

### Example 1: Full CRUD Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    void fullCrudLifecycle_createReadUpdateDelete() throws Exception {
        // 1. CREATE
        ItemCreateRequest createRequest = new ItemCreateRequest(
            "Laptop",
            "Electronics",
            "Gaming laptop"
        );
        
        String createResponse = mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        ItemResponse created = objectMapper.readValue(createResponse, ItemResponse.class);
        Long itemId = created.id();
        
        // Verify in database
        assertThat(itemRepository.findById(itemId)).isPresent();
        
        // 2. READ
        mockMvc.perform(get("/api/items/{id}", itemId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.category").value("Electronics"));
        
        // 3. UPDATE
        ItemUpdateRequest updateRequest = new ItemUpdateRequest(
            "Updated Laptop",
            "Gaming",
            "High-end gaming laptop"
        );
        
        mockMvc.perform(put("/api/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Laptop"));
        
        // Verify update in database
        Item updated = itemRepository.findById(itemId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Laptop");
        assertThat(updated.getCategory()).isEqualTo("Gaming");
        
        // 4. DELETE
        mockMvc.perform(delete("/api/items/{id}", itemId))
            .andExpect(status().isNoContent());
        
        // Verify deletion in database
        assertThat(itemRepository.findById(itemId)).isEmpty();
        
        // 5. READ after DELETE - should return 404
        mockMvc.perform(get("/api/items/{id}", itemId))
            .andExpect(status().isNotFound());
    }
}
```

---

### Example 2: Testing Complex Business Flow

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShoppingListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create authenticated user
        testUser = userRepository.save(
            User.builder()
                .username("shopper")
                .email("shopper@example.com")
                .password("encoded")
                .build()
        );
        
        jwtToken = jwtService.generateToken(testUser);
    }

    @Test
    void completeShoppingListFlow_createAddItemsComplete() throws Exception {
        // 1. Create shopping list
        ShoppingListCreateRequest listRequest = new ShoppingListCreateRequest("Groceries");
        
        String listResponse = mockMvc.perform(post("/api/shopping-lists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        ShoppingListResponse list = objectMapper.readValue(listResponse, ShoppingListResponse.class);
        
        // 2. Create items to add
        Item milk = itemRepository.save(Item.builder().name("Milk").category("Dairy").build());
        Item bread = itemRepository.save(Item.builder().name("Bread").category("Bakery").build());
        
        // 3. Add items to shopping list
        AddItemRequest addMilk = new AddItemRequest(milk.getId(), 2);
        mockMvc.perform(post("/api/shopping-lists/{id}/items", list.id())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addMilk)))
            .andExpect(status().isOk());
        
        AddItemRequest addBread = new AddItemRequest(bread.getId(), 1);
        mockMvc.perform(post("/api/shopping-lists/{id}/items", list.id())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addBread)))
            .andExpect(status().isOk());
        
        // 4. Verify items were added
        mockMvc.perform(get("/api/shopping-lists/{id}", list.id())
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.items[*].item.name", hasItems("Milk", "Bread")))
            .andExpect(jsonPath("$.items[0].quantity").exists());
        
        // 5. Mark list as completed
        mockMvc.perform(put("/api/shopping-lists/{id}/complete", list.id())
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk());
        
        // 6. Verify status in database
        ShoppingList completed = shoppingListRepository.findById(list.id()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test
    void addItemToShoppingList_unauthorizedUser_returnsForbidden() throws Exception {
        // Create list with user1
        ShoppingList list = shoppingListRepository.save(
            ShoppingList.builder()
                .name("User1's List")
                .user(testUser)
                .build()
        );
        
        // Create different user
        User otherUser = userRepository.save(
            User.builder()
                .username("other")
                .email("other@example.com")
                .password("encoded")
                .build()
        );
        String otherToken = jwtService.generateToken(otherUser);
        
        // Try to add item with user2's token
        Item item = itemRepository.save(Item.builder().name("Item").build());
        AddItemRequest request = new AddItemRequest(item.getId(), 1);
        
        mockMvc.perform(post("/api/shopping-lists/{id}/items", list.getId())
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
```

---

## Testing Best Practices

### 1. Test Data Management

```java
/**
 * Use helper methods to create test data.
 */
public class TestDataBuilder {
    
    public static Item createTestItem(String name) {
        return Item.builder()
            .name(name)
            .category("Test Category")
            .description("Test description")
            .build();
    }
    
    public static ItemCreateRequest createItemRequest(String name) {
        return new ItemCreateRequest(
            name,
            "Test Category",
            "Test description"
        );
    }
    
    public static User createTestUser(String username) {
        return User.builder()
            .username(username)
            .email(username + "@example.com")
            .password("encoded-password")
            .role(Role.USER)
            .build();
    }
}

// Usage in tests
@Test
void test() {
    Item item = TestDataBuilder.createTestItem("Laptop");
    itemRepository.save(item);
}
```

---

### 2. Extract Common Setup

```java
/**
 * Use @BeforeEach for common setup across tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ItemIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    private Item savedItem;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        
        // Common test data
        savedItem = itemRepository.save(
            TestDataBuilder.createTestItem("Common Item")
        );
    }

    @Test
    void test1() {
        // savedItem is available
    }

    @Test
    void test2() {
        // savedItem is available (fresh copy per test due to @Transactional)
    }
}
```

---

### 3. Use Descriptive Test Names

```java
// ❌ BAD
@Test
void test1() { }

@Test
void testCreate() { }

// ✅ GOOD
@Test
void createItem_validRequest_returnsCreatedStatus() { }

@Test
void createItem_duplicateName_returnsConflictError() { }

@Test
void createItem_blankName_returnsBadRequestWithValidationError() { }
```

---

### 4. Test One Thing Per Test

```java
// ❌ BAD - Testing multiple scenarios in one test
@Test
void testAllItemScenarios() {
    // Test create
    // Test update
    // Test delete
    // Test validation
    // ... way too much
}

// ✅ GOOD - Separate tests
@Test
void createItem_validRequest_returnsCreated() { }

@Test
void updateItem_validRequest_returnsOk() { }

@Test
void deleteItem_existingId_returnsNoContent() { }

@Test
void createItem_invalidRequest_returnsBadRequest() { }
```

---

### 5. Use @Sql for Complex Test Data

```java
@SpringBootTest
@AutoConfigureMockMvc
class ItemIntegrationTest {

    @Test
    @Sql("/test-data/items.sql")  // Load SQL script before test
    void searchItems_withMultipleItems_returnsFiltered() throws Exception {
        // Database already populated with items.sql
        mockMvc.perform(get("/api/items/search")
                .param("category", "Electronics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @Sql(scripts = "/test-data/items.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_withSetupAndCleanup() {
        // items.sql runs before
        // cleanup.sql runs after
    }
}
```

---

### 6. Use @DirtiesContext Sparingly

```java
/**
 * ⚠️ Use @DirtiesContext only when absolutely necessary.
 * It forces Spring to reload the entire context, making tests VERY slow.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SlowTest {
    // This will reload context after EACH test - very slow
}

// Instead, use @Transactional to rollback changes
@SpringBootTest
@Transactional  // Auto-rollback, no context reload needed
class FastTest {
    // Much faster
}
```

---

## Common Patterns & Solutions

### Pattern 1: Testing File Uploads

```java
@Test
void uploadItemImage_validFile_returnsSuccess() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "laptop.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "image content".getBytes()
    );
    
    mockMvc.perform(multipart("/api/items/{id}/image", 1L)
            .file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imageUrl").exists());
}

@Test
void uploadItemImage_invalidFileType_returnsBadRequest() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "document.pdf",
        MediaType.APPLICATION_PDF_VALUE,
        "pdf content".getBytes()
    );
    
    mockMvc.perform(multipart("/api/items/{id}/image", 1L)
            .file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Invalid file type")));
}
```

---

### Pattern 2: Testing Async Operations

```java
@Test
void processOrder_validOrder_triggersAsyncNotification() throws Exception {
    OrderRequest request = new OrderRequest(1L, 5);
    
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    
    // Wait for async operation
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
        });
}
```

---

### Pattern 3: Testing Pagination and Sorting

```java
@Test
void getAllItems_withPaginationAndSorting_returnsCorrectPage() throws Exception {
    // Arrange - Create multiple items
    List<Item> items = IntStream.range(1, 11)
        .mapToObj(i -> Item.builder()
            .name("Item " + i)
            .category("Category")
            .build())
        .toList();
    itemRepository.saveAll(items);
    
    // Act & Assert - Request page 2, size 3, sorted by name descending
    mockMvc.perform(get("/api/items")
            .param("page", "1")       // Zero-indexed: page 1 = second page
            .param("size", "3")
            .param("sort", "name,desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.content[0].name").value("Item 7"))  // Descending
        .andExpect(jsonPath("$.totalElements").value(10))
        .andExpect(jsonPath("$.totalPages").value(4))
        .andExpect(jsonPath("$.number").value(1))
        .andExpect(jsonPath("$.size").value(3))
        .andExpect(jsonPath("$.first").value(false))
        .andExpect(jsonPath("$.last").value(false));
}
```

---

### Pattern 4: Testing Error Response Format

```java
@Test
void handleException_returnsConsistentErrorFormat() throws Exception {
    when(itemService.getItemById(999L))
        .thenThrow(new ResourceNotFoundException("Item not found"));
    
    mockMvc.perform(get("/api/items/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Item not found"))
        .andExpect(jsonPath("$.path").value("/api/items/999"))
        .andExpect(jsonPath("$.errors").doesNotExist());  // No validation errors
}

@Test
void handleValidationException_returnsErrorsMap() throws Exception {
    ItemCreateRequest request = new ItemCreateRequest("", "", "");
    
    mockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.name").exists())
        .andExpect(jsonPath("$.errors.category").exists());
}
```

---

## Anti-Patterns to Avoid

### ❌ 1. Over-Testing with Integration Tests

```java
// ❌ BAD - Testing simple getters with integration test
@SpringBootTest
class UserIntegrationTest {
    @Test
    void getUserName_returnsName() {
        User user = new User();
        user.setName("John");
        assertThat(user.getName()).isEqualTo("John");
    }
}

// ✅ GOOD - Use unit tests for simple logic, integration tests for flows
@Test  // Plain JUnit test, no Spring context
void getUserName_returnsName() {
    User user = new User();
    user.setName("John");
    assertThat(user.getName()).isEqualTo("John");
}
```

**Principle:** Integration tests are expensive. Use them for integration, not simple logic.

---

### ❌ 2. Not Cleaning Up Test Data

```java
// ❌ BAD - Tests interfere with each other
@SpringBootTest
class ItemTest {
    @Test
    void test1() {
        itemRepository.save(item);  // Data persists
    }
    
    @Test
    void test2() {
        List<Item> items = itemRepository.findAll();
        // Might include data from test1!
    }
}

// ✅ GOOD - Use @Transactional or @BeforeEach cleanup
@SpringBootTest
@Transactional  // Auto-rollback after each test
class ItemTest {
    @Test
    void test1() {
        itemRepository.save(item);
    }
    
    @Test
    void test2() {
        List<Item> items = itemRepository.findAll();
        // Always starts with clean database
    }
}
```

---

### ❌ 3. Testing Too Much in Controller Tests

```java
// ❌ BAD - Testing business logic in controller test
@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Test
    void createItem_calculatesDiscountCorrectly() {
        // Controller test shouldn't test discount calculation
        // That's service layer logic
    }
}

// ✅ GOOD - Controller tests verify HTTP contract only
@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Test
    void createItem_validRequest_callsServiceAndReturns201() {
        // Just verify controller calls service and returns correct status
        mockMvc.perform(post("/api/items")...)
            .andExpect(status().isCreated());
        
        verify(itemService).createItem(any());
    }
}
```

---

### ❌ 4. Hardcoding Test Data

```java
// ❌ BAD - Hardcoded IDs break when database state changes
@Test
void getItem() {
    mockMvc.perform(get("/api/items/{id}", 42))  // Magic number
        .andExpect(status().isOk());
}

// ✅ GOOD - Create and use dynamic test data
@Test
void getItem() {
    Item saved = itemRepository.save(TestDataBuilder.createTestItem());
    
    mockMvc.perform(get("/api/items/{id}", saved.getId()))
        .andExpect(status().isOk());
}
```

---

### ❌ 5. Not Testing Error Cases

```java
// ❌ BAD - Only testing happy path
@Test
void createItem() {
    mockMvc.perform(post("/api/items")...)
        .andExpect(status().isCreated());
}

// ✅ GOOD - Test both success and failure
@Test
void createItem_validRequest_returnsCreated() { }

@Test
void createItem_blankName_returnsBadRequest() { }

@Test
void createItem_duplicateName_returnsConflict() { }

@Test
void createItem_unauthorized_returns401() { }
```

---

### ❌ 6. Slow Tests Due to Full Context Loading

```java
// ❌ BAD - Loading full context for simple controller test
@SpringBootTest  // Loads EVERYTHING
@AutoConfigureMockMvc
class ItemControllerTest {
    // 5 seconds startup time for each test class
}

// ✅ GOOD - Use @WebMvcTest for controller tests
@WebMvcTest(ItemController.class)  // Only web layer
class ItemControllerTest {
    // 500ms startup time
}
```

---

## Quick Reference

### When to Use What

| Test Type | Annotation | Use When | Speed | Database |
|-----------|-----------|----------|-------|----------|
| **Unit** | `@ExtendWith(MockitoExtension.class)` | Testing service/repository logic | ⚡ Very Fast | ❌ No |
| **Controller** | `@WebMvcTest` | Testing web layer only | ⚡ Fast | ❌ No |
| **Integration** | `@SpringBootTest` | Testing full flow | 🐢 Slow | ✅ Yes |
| **Repository** | `@DataJpaTest` | Testing JPA queries | ⚡ Fast | ✅ Yes (in-memory) |

---

### Common Annotations Cheat Sheet

```java
// Controller Tests
@WebMvcTest(ControllerClass.class)       // Load only web layer
@Import(SecurityConfig.class)            // Import security config
@WithMockUser                            // Simulate authenticated user
@WithMockUser(roles = "ADMIN")          // Simulate user with role

// Integration Tests
@SpringBootTest                          // Load full context
@AutoConfigureMockMvc                    // Configure MockMvc
@ActiveProfiles("test")                  // Use test profile
@Transactional                           // Auto-rollback
@Sql("/data.sql")                        // Load SQL script
@DirtiesContext                          // Reload context (use sparingly)

// TestContainers
@Testcontainers                          // Enable TestContainers
@Container                               // Mark container field
```

---

### MockMvc Cheat Sheet

```java
// Perform requests
mockMvc.perform(get("/api/items"))
mockMvc.perform(post("/api/items").content(...))
mockMvc.perform(put("/api/items/{id}", 1L))
mockMvc.perform(delete("/api/items/{id}", 1L))

// With parameters
.param("page", "0")
.param("size", "10")

// With headers
.header("Authorization", "Bearer " + token)
.contentType(MediaType.APPLICATION_JSON)

// Expect status
.andExpect(status().isOk())
.andExpect(status().isCreated())
.andExpect(status().isBadRequest())
.andExpect(status().isNotFound())
.andExpect(status().isUnauthorized())
.andExpect(status().isForbidden())

// Expect JSON
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.name").value("Laptop"))
.andExpect(jsonPath("$", hasSize(3)))
.andExpect(jsonPath("$[0].id").exists())

// Get response
.andReturn().getResponse().getContentAsString()
```

---

## Final Tips

1. **Start with controller tests** - They're faster and catch most issues
2. **Use integration tests for critical paths** - Don't test everything
3. **Keep tests independent** - Each test should work in isolation
4. **Use @Transactional for auto-cleanup** - Avoid manual cleanup
5. **Mock external dependencies** - APIs, email services, payment gateways
6. **Test security** - Don't forget authentication/authorization
7. **Test validation** - Both bean validation and business rules
8. **Name tests descriptively** - `methodName_scenario_expectedResult`
9. **Use test data builders** - Make tests readable and maintainable
10. **Run tests often** - Catch issues early

---

## Next Steps

1. **Practice**: Write controller tests for your existing endpoints
2. **Add integration tests**: Focus on critical business flows
3. **Refactor**: Improve test readability using patterns from this guide
4. **Security**: Add security testing to protected endpoints
5. **CI/CD**: Automate test execution in your pipeline

Remember: **Good tests are your safety net.** They give you confidence to refactor, prevent regressions, and document how your API actually works.

---

*Happy Testing! 🧪*
