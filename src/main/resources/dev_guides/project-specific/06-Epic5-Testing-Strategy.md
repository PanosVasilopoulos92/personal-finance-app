# 🧪 Epic 5: Testing & Quality Assurance

> **Duration:** Ongoing (integrated with each story) | **Stories:** 5.1, 5.2, 5.3  
> **Goal:** Build confidence through comprehensive testing

---

## 📋 Epic Overview

Testing isn't a phase - it's a **continuous practice**. This guide teaches you to write tests that:
- Catch bugs before production
- Document expected behavior
- Enable fearless refactoring
- Build deployment confidence

**The Testing Mindset:**
```
Without tests: "I think it works..."
With tests:    "I KNOW it works!"
```

---

## 🎯 The Testing Pyramid

```
                    /\
                   /  \      E2E Tests (5%)
                  /----\     - Full user flows
                 /      \    - Slowest, most expensive
                /--------\
               /          \  Integration Tests (20%)
              /  API Tests \  - Multiple components
             /--------------\ - Real database (test containers)
            /                \
           /   Unit Tests     \ Unit Tests (75%)
          /    (Foundation)    \ - Single component, mocked deps
         /----------------------\ - Fast, isolated
```

**Our Strategy:**
- 75% Unit Tests (Services, Utilities)
- 20% Integration Tests (Repositories, API)
- 5% E2E Tests (Critical flows)

---

## Story 5.1: Unit Test Coverage

**As a** developer  
**I want** comprehensive unit tests for all services  
**So that** I can refactor with confidence

### What Makes a Good Unit Test?

1. **Fast** - Runs in milliseconds
2. **Isolated** - No external dependencies (database, network)
3. **Repeatable** - Same result every time
4. **Self-validating** - Pass/fail, no manual inspection
5. **Timely** - Written alongside the code

---

### Step 1: Test Dependencies

Ensure these are in your `pom.xml`:

```xml
<!-- Testing (included in spring-boot-starter-test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Additional test utilities -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

### Step 2: Understanding Test Annotations

```java
// JUnit 5 Annotations
@Test                    // Marks a method as a test
@DisplayName("...")      // Human-readable test name
@BeforeEach              // Runs before EACH test method
@AfterEach               // Runs after EACH test method
@BeforeAll               // Runs ONCE before all tests (static)
@AfterAll                // Runs ONCE after all tests (static)
@Disabled                // Skip this test

// Mockito Annotations
@ExtendWith(MockitoExtension.class)  // Enable Mockito
@Mock                    // Create a mock object
@InjectMocks             // Inject mocks into the tested class
@Spy                     // Partial mock (real methods unless stubbed)
@Captor                  // Capture arguments passed to mocks
```

---

### Step 3: The AAA Pattern

Every test follows **Arrange → Act → Assert**:

```java
@Test
@DisplayName("getUserById - when user exists - should return UserResponse")
void getUserById_WhenUserExists_ReturnsUserResponse() {
    // ========== ARRANGE ==========
    // Set up test data and mock behavior
    User testUser = createTestUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    
    // ========== ACT ==========
    // Call the method being tested
    UserResponse response = userService.getUserById(1L);
    
    // ========== ASSERT ==========
    // Verify the results
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.email()).isEqualTo("test@example.com");
    
    // Verify mock interactions
    verify(userRepository, times(1)).findById(1L);
}
```

---

### Step 4: Complete Service Test Example

```java
/**
 * Unit tests for UserService.
 * 
 * Test Strategy:
 * - Mock all dependencies (UserRepository, PasswordEncoder)
 * - Test each public method
 * - Cover happy paths AND error paths
 * - Verify business rules are enforced
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    // Test data
    private User testUser;
    private CreateUserRequest validCreateRequest;
    
    @BeforeEach
    void setUp() {
        // Initialize test data before each test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john@example.com");
        testUser.setPassword("encrypted_password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUserRole(UserRolesEnum.USER);
        testUser.setActive(true);
        
        validCreateRequest = new CreateUserRequest(
            "johndoe",
            "john@example.com",
            "John",
            "Doe",
            "SecurePass123!",
            25
        );
    }
    
    // ==================== READ OPERATIONS ====================
    
    @Nested
    @DisplayName("getUserById")
    class GetUserById {
        
        @Test
        @DisplayName("when user exists - should return UserResponse")
        void whenUserExists_ReturnsUserResponse() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            
            // Act
            UserResponse response = userService.getUserById(1L);
            
            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("john@example.com");
            assertThat(response.firstName()).isEqualTo("John");
            
            verify(userRepository).findById(1L);
        }
        
        @Test
        @DisplayName("when user not found - should throw ResourceNotFoundException")
        void whenUserNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("999");
            
            verify(userRepository).findById(999L);
        }
    }
    
    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmail {
        
        @Test
        @DisplayName("when user exists - should return UserResponse")
        void whenUserExists_ReturnsUserResponse() {
            // Arrange
            when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));
            
            // Act
            UserResponse response = userService.getUserByEmail("john@example.com");
            
            // Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("john@example.com");
        }
        
        @Test
        @DisplayName("when email not found - should throw ResourceNotFoundException")
        void whenEmailNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> userService.getUserByEmail("unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
    
    // ==================== WRITE OPERATIONS ====================
    
    @Nested
    @DisplayName("registerUser")
    class RegisterUser {
        
        @Test
        @DisplayName("when valid request - should create user")
        void whenValidRequest_CreatesUser() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encrypted");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            
            // Act
            UserResponse response = userService.registerUser(validCreateRequest);
            
            // Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("john@example.com");
            
            // Verify interactions
            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).existsByUsername("johndoe");
            verify(passwordEncoder).encode("SecurePass123!");
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("when email exists - should throw DuplicateResourceException")
        void whenEmailExists_ThrowsDuplicateResourceException() {
            // Arrange
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(validCreateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
            
            // Verify save was NOT called
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("when username exists - should throw DuplicateResourceException")
        void whenUsernameExists_ThrowsDuplicateResourceException() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(validCreateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already taken");
            
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("when age under 13 - should throw BusinessValidationException")
        void whenAgeUnder13_ThrowsBusinessValidationException() {
            // Arrange
            CreateUserRequest underageRequest = new CreateUserRequest(
                "youngling",
                "young@example.com",
                "Young",
                "User",
                "SecurePass123!",
                12  // Under 13
            );
            
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(underageRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("at least 13 years old");
        }
    }
    
    @Nested
    @DisplayName("updateUser")
    class UpdateUser {
        
        @Test
        @DisplayName("when valid update - should update user fields")
        void whenValidUpdate_UpdatesUserFields() {
            // Arrange
            UpdateUserRequest updateRequest = new UpdateUserRequest(
                "UpdatedFirst",
                "UpdatedLast",
                30
            );
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            
            // Act
            UserResponse response = userService.updateUser(1L, updateRequest);
            
            // Assert
            assertThat(response.firstName()).isEqualTo("UpdatedFirst");
            assertThat(response.lastName()).isEqualTo("UpdatedLast");
            
            // Verify the entity was modified (dirty checking handles save)
            assertThat(testUser.getFirstName()).isEqualTo("UpdatedFirst");
        }
        
        @Test
        @DisplayName("when user not found - should throw ResourceNotFoundException")
        void whenUserNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> userService.updateUser(999L, any()))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
    
    @Nested
    @DisplayName("changePassword")
    class ChangePassword {
        
        @Test
        @DisplayName("when current password correct - should update password")
        void whenCurrentPasswordCorrect_UpdatesPassword() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass123!",
                "NewPass456!"
            );
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("OldPass123!", testUser.getPassword()))
                .thenReturn(true);
            when(passwordEncoder.encode("NewPass456!")).thenReturn("new_encrypted");
            
            // Act
            userService.changePassword(1L, request);
            
            // Assert
            assertThat(testUser.getPassword()).isEqualTo("new_encrypted");
            verify(passwordEncoder).encode("NewPass456!");
        }
        
        @Test
        @DisplayName("when current password incorrect - should throw InvalidPasswordException")
        void whenCurrentPasswordIncorrect_ThrowsInvalidPasswordException() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPassword",
                "NewPass456!"
            );
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPassword", testUser.getPassword()))
                .thenReturn(false);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Current password is incorrect");
            
            // Verify new password was NOT encoded
            verify(passwordEncoder, never()).encode(anyString());
        }
    }
    
    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {
        
        @Test
        @DisplayName("should soft delete user by setting active to false")
        void shouldSoftDeleteUser() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            
            // Act
            userService.deleteUser(1L);
            
            // Assert - User should be deactivated, not removed
            assertThat(testUser.getActive()).isFalse();
            verify(userRepository, never()).delete(any(User.class));
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    // You can add helper methods for creating test data
    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encrypted");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUserRole(UserRolesEnum.USER);
        user.setActive(true);
        return user;
    }
}
```

---

### Step 5: Testing Edge Cases

Always test edge cases:

```java
@Nested
@DisplayName("Edge Cases")
class EdgeCases {
    
    @Test
    @DisplayName("searchUsers - when search term is null - should return empty list")
    void searchUsers_WhenTermNull_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers(null);
        
        // Assert
        assertThat(results).isEmpty();
        verify(userRepository, never()).searchByName(anyString());
    }
    
    @Test
    @DisplayName("searchUsers - when search term is empty - should return empty list")
    void searchUsers_WhenTermEmpty_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers("   ");
        
        // Assert
        assertThat(results).isEmpty();
    }
    
    @Test
    @DisplayName("searchUsers - when search term is whitespace - should return empty list")
    void searchUsers_WhenTermWhitespace_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers("   ");
        
        // Assert
        assertThat(results).isEmpty();
    }
}
```

---

### Story 5.1 Checklist

- [ ] Test class for every service
- [ ] @ExtendWith(MockitoExtension.class) added
- [ ] All dependencies mocked
- [ ] @BeforeEach sets up test data
- [ ] Tests organized with @Nested classes
- [ ] Descriptive @DisplayName on all tests
- [ ] Happy paths tested
- [ ] Error paths tested
- [ ] Edge cases tested
- [ ] Mock interactions verified

---

## Story 5.2: Integration Test Suite

**As a** developer  
**I want** integration tests for API endpoints  
**So that** I verify the full request/response cycle

### What Integration Tests Cover

```
HTTP Request → Controller → Service → Repository → Database
     ↑                                                 |
     └─────────────── Response ←───────────────────────┘
```

---

### Step 1: MockMvc Setup

```java
/**
 * Integration tests for UserController.
 * 
 * @WebMvcTest loads only the web layer (Controllers, Filters, etc.)
 * MockMvc simulates HTTP requests without starting a server
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean  // Spring mock (not Mockito @Mock!)
    private UserService userService;
    
    private UserResponse testUserResponse;
    
    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponse(
            1L,
            "john@example.com",
            "johndoe",
            "John",
            "Doe",
            "John Doe",
            UserRolesEnum.USER,
            25,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    // ==================== GET OPERATIONS ====================
    
    @Test
    @DisplayName("GET /api/users/{id} - when user exists - should return 200 OK")
    void getUserById_WhenExists_Returns200() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUserResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"));
        
        verify(userService).getUserById(1L);
    }
    
    @Test
    @DisplayName("GET /api/users/{id} - when user not found - should return 404")
    void getUserById_WhenNotFound_Returns404() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
            .thenThrow(new ResourceNotFoundException("User not found: 999"));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found: 999"));
    }
    
    @Test
    @DisplayName("GET /api/users - should return paginated list")
    void getAllUsers_Returns200WithPaginatedList() throws Exception {
        // Arrange
        Page<UserResponse> page = new PageImpl<>(
            List.of(testUserResponse),
            PageRequest.of(0, 20),
            1
        );
        when(userService.getAllUsers(0, 20, "id", "ASC")).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    // ==================== POST OPERATIONS ====================
    
    @Test
    @DisplayName("POST /api/users - with valid data - should return 201 Created")
    void createUser_WithValidData_Returns201() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
            "johndoe",
            "john@example.com",
            "John",
            "Doe",
            "SecurePass123!",
            25
        );
        when(userService.registerUser(any(CreateUserRequest.class)))
            .thenReturn(testUserResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
    
    @Test
    @DisplayName("POST /api/users - with invalid email - should return 400")
    void createUser_WithInvalidEmail_Returns400() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
            "johndoe",
            "invalid-email",  // Invalid!
            "John",
            "Doe",
            "SecurePass123!",
            25
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.email").exists());
        
        // Service should NOT be called
        verify(userService, never()).registerUser(any());
    }
    
    @Test
    @DisplayName("POST /api/users - with blank username - should return 400")
    void createUser_WithBlankUsername_Returns400() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
            "",  // Blank!
            "john@example.com",
            "John",
            "Doe",
            "SecurePass123!",
            25
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.username").exists());
    }
    
    @Test
    @DisplayName("POST /api/users - with duplicate email - should return 409")
    void createUser_WithDuplicateEmail_Returns409() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
            "johndoe",
            "existing@example.com",
            "John",
            "Doe",
            "SecurePass123!",
            25
        );
        when(userService.registerUser(any()))
            .thenThrow(new DuplicateResourceException("Email already registered"));
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email already registered"));
    }
    
    // ==================== PUT OPERATIONS ====================
    
    @Test
    @DisplayName("PUT /api/users/{id} - with valid data - should return 200")
    void updateUser_WithValidData_Returns200() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(
            "UpdatedFirst",
            "UpdatedLast",
            30
        );
        UserResponse updatedResponse = new UserResponse(
            1L, "john@example.com", "johndoe",
            "UpdatedFirst", "UpdatedLast", "UpdatedFirst UpdatedLast",
            UserRolesEnum.USER, 30,
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.updateUser(eq(1L), any())).thenReturn(updatedResponse);
        
        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("UpdatedFirst"));
    }
    
    // ==================== DELETE OPERATIONS ====================
    
    @Test
    @DisplayName("DELETE /api/users/{id} - should return 204 No Content")
    void deleteUser_Returns204() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(1L);
    }
}
```

---

### Step 2: Full Integration Tests with Real Database

For tests that need a real database, use `@SpringBootTest`:

```java
/**
 * Full integration tests with real database.
 * Uses TestContainers for a real MySQL instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserIntegrationTest {
    
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Full registration flow - should create user and return JWT")
    void fullRegistrationFlow() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "johndoe",
            "john@example.com",
            "SecurePass123!",
            "John",
            "Doe",
            25
        );
        
        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            AuthResponse.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().email()).isEqualTo("john@example.com");
        
        // Verify database state
        Optional<User> savedUser = userRepository.findByEmail("john@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("John");
    }
    
    @Test
    @DisplayName("Protected endpoint - without token - should return 401")
    void protectedEndpoint_WithoutToken_Returns401() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/users/me",
            String.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("Protected endpoint - with valid token - should return 200")
    void protectedEndpoint_WithValidToken_Returns200() {
        // Arrange - Create user and get token
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("SecurePass123!"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserRole(UserRolesEnum.USER);
        user.setActive(true);
        userRepository.save(user);
        
        // Login to get token
        LoginRequest loginRequest = new LoginRequest(
            "john@example.com",
            "SecurePass123!"
        );
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            AuthResponse.class
        );
        String token = authResponse.getBody().token();
        
        // Act - Access protected endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<UserResponse> response = restTemplate.exchange(
            "/api/users/me",
            HttpMethod.GET,
            entity,
            UserResponse.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo("john@example.com");
    }
}
```

---

### Story 5.2 Checklist

- [ ] @WebMvcTest for each controller
- [ ] MockMvc for HTTP simulation
- [ ] @MockBean for service dependencies
- [ ] Test all HTTP methods (GET, POST, PUT, DELETE)
- [ ] Test success responses (200, 201, 204)
- [ ] Test error responses (400, 401, 403, 404, 409)
- [ ] Test validation errors
- [ ] Test authentication/authorization
- [ ] Full integration tests with TestContainers

---

## Story 5.3: Repository Tests

**As a** developer  
**I want** to test custom repository queries  
**So that** data access works correctly

### @DataJpaTest Explained

```java
@DataJpaTest
// This annotation:
// ✅ Configures in-memory H2 database
// ✅ Scans for @Entity classes
// ✅ Configures Spring Data JPA repositories
// ✅ Each test runs in a transaction (rollback after)
// ❌ Does NOT load controllers, services, etc.
```

---

### Complete Repository Test Example

```java
/**
 * Repository tests for UserRepository.
 * 
 * Tests custom query methods to ensure they work correctly.
 * Uses TestEntityManager to set up test data.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = new User();
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encrypted_password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUserRole(UserRolesEnum.USER);
        testUser.setActive(true);
        
        entityManager.persist(testUser);
        entityManager.flush();
    }
    
    // ==================== FIND BY EMAIL ====================
    
    @Test
    @DisplayName("findByEmail - when email exists - should return user")
    void findByEmail_WhenExists_ReturnsUser() {
        // Act
        Optional<User> found = userRepository.findByEmail("john@example.com");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }
    
    @Test
    @DisplayName("findByEmail - when email not found - should return empty")
    void findByEmail_WhenNotFound_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("unknown@example.com");
        
        // Assert
        assertThat(found).isEmpty();
    }
    
    // ==================== EXISTS BY EMAIL ====================
    
    @Test
    @DisplayName("existsByEmail - when email exists - should return true")
    void existsByEmail_WhenExists_ReturnsTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("john@example.com");
        
        // Assert
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("existsByEmail - when email not found - should return false")
    void existsByEmail_WhenNotFound_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("unknown@example.com");
        
        // Assert
        assertThat(exists).isFalse();
    }
    
    // ==================== SEARCH BY NAME ====================
    
    @Test
    @DisplayName("searchByName - with matching first name - should return users")
    void searchByName_WithMatchingFirstName_ReturnsUsers() {
        // Arrange - Add another user
        User anotherUser = new User();
        anotherUser.setUsername("johnsmith");
        anotherUser.setEmail("johnsmith@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFirstName("Johnny");
        anotherUser.setLastName("Smith");
        anotherUser.setUserRole(UserRolesEnum.USER);
        anotherUser.setActive(true);
        entityManager.persist(anotherUser);
        entityManager.flush();
        
        // Act - Case-insensitive search
        List<User> results = userRepository.searchByName("john");
        
        // Assert
        assertThat(results).hasSize(2);
        assertThat(results)
            .extracting(User::getFirstName)
            .containsExactlyInAnyOrder("John", "Johnny");
    }
    
    @Test
    @DisplayName("searchByName - with no matches - should return empty list")
    void searchByName_WithNoMatches_ReturnsEmptyList() {
        // Act
        List<User> results = userRepository.searchByName("xyz");
        
        // Assert
        assertThat(results).isEmpty();
    }
    
    // ==================== JOIN FETCH QUERIES ====================
    
    @Test
    @DisplayName("findByIdWithPreferences - should load preferences without lazy exception")
    void findByIdWithPreferences_LoadsPreferences() {
        // Arrange - Add preferences
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(testUser);
        preferences.setCurrency("USD");
        preferences.setLanguage("en");
        testUser.setPreferences(preferences);
        entityManager.persist(preferences);
        entityManager.flush();
        entityManager.clear();  // Clear persistence context!
        
        // Act
        Optional<User> found = userRepository.findByIdWithPreferences(testUser.getId());
        
        // Assert - This should NOT throw LazyInitializationException
        assertThat(found).isPresent();
        assertThat(found.get().getPreferences()).isNotNull();
        assertThat(found.get().getPreferences().getCurrency()).isEqualTo("USD");
    }
    
    // ==================== PAGINATION ====================
    
    @Test
    @DisplayName("findByActive - with pagination - should return page")
    void findByActive_WithPagination_ReturnsPage() {
        // Arrange - Create 15 more users
        for (int i = 0; i < 15; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setFirstName("User");
            user.setLastName(String.valueOf(i));
            user.setUserRole(UserRolesEnum.USER);
            user.setActive(true);
            entityManager.persist(user);
        }
        entityManager.flush();
        
        // Act - Get first page of 10
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username").ascending());
        Page<User> page = userRepository.findByActive(true, pageable);
        
        // Assert
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(16);  // 15 + testUser
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }
    
    // ==================== COUNT QUERIES ====================
    
    @Test
    @DisplayName("countByActive - should return correct counts")
    void countByActive_ReturnsCorrectCounts() {
        // Arrange - Create inactive user
        User inactiveUser = new User();
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setUserRole(UserRolesEnum.USER);
        inactiveUser.setActive(false);
        entityManager.persist(inactiveUser);
        entityManager.flush();
        
        // Act
        long activeCount = userRepository.countByActive(true);
        long inactiveCount = userRepository.countByActive(false);
        
        // Assert
        assertThat(activeCount).isEqualTo(1);  // testUser
        assertThat(inactiveCount).isEqualTo(1);  // inactiveUser
    }
}
```

---

### Testing N+1 Query Problem

```java
@Test
@DisplayName("findAllWithItems - should not cause N+1 queries")
void findAllWithItems_NoNPlusOneQueries() {
    // Arrange - Create user with multiple items
    for (int i = 0; i < 5; i++) {
        Item item = new Item();
        item.setName("Item " + i);
        item.setUser(testUser);
        entityManager.persist(item);
    }
    entityManager.flush();
    entityManager.clear();
    
    // Act - This should use JOIN FETCH
    List<User> users = userRepository.findAllWithItems();
    
    // Assert - Access items without lazy exception
    // If this doesn't throw, JOIN FETCH is working
    users.forEach(user -> {
        assertThat(user.getItems()).isNotEmpty();
        user.getItems().forEach(item -> {
            assertThat(item.getName()).isNotNull();
        });
    });
}
```

---

### Story 5.3 Checklist

- [ ] @DataJpaTest for each repository
- [ ] TestEntityManager for test data setup
- [ ] Test findBy... query methods
- [ ] Test existsBy... query methods
- [ ] Test @Query custom queries
- [ ] Test JOIN FETCH queries (no LazyInitializationException)
- [ ] Test pagination and sorting
- [ ] Test count queries
- [ ] Test update and delete queries

---

## 📊 Test Coverage with JaCoCo

### Configure JaCoCo Plugin

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Generate Coverage Report

```bash
# Run tests and generate report
mvn clean test

# View report at:
# target/site/jacoco/index.html
```

### Coverage Goals

| Layer | Target | Priority |
|-------|--------|----------|
| Services | 90%+ | High |
| Controllers | 85%+ | High |
| Repositories | 80%+ | Medium |
| DTOs | Not needed | - |
| Entities | Not needed | - |

---

## 🎯 Testing Best Practices Summary

### DO ✅

```java
// 1. Use descriptive test names
@DisplayName("registerUser - when email exists - should throw DuplicateResourceException")

// 2. Follow AAA pattern
// Arrange - Act - Assert

// 3. One assertion concept per test
void shouldReturnUserWhenIdExists() { ... }
void shouldThrowWhenIdNotFound() { ... }

// 4. Use AssertJ for readable assertions
assertThat(response).isNotNull();
assertThat(list).hasSize(5);
assertThat(list).extracting(User::getName).contains("John");

// 5. Test edge cases
void whenInputNull_shouldReturnEmpty() { ... }
void whenInputEmpty_shouldReturnEmpty() { ... }
```

### DON'T ❌

```java
// 1. Don't test multiple things in one test
void testUserOperations() {
    // create, read, update, delete...  ❌ Too much!
}

// 2. Don't use generic names
void test1() { ... }  ❌

// 3. Don't share state between tests
static User sharedUser;  ❌

// 4. Don't test framework code
void testSpringDataSave() {
    repository.save(entity);
    // Testing Spring Data, not your code ❌
}

// 5. Don't ignore test failures
@Disabled  // "Fix later" ❌
void brokenTest() { ... }
```

---

## 🎉 Epic 5 Complete!

You now have:
- Comprehensive unit tests for services
- Integration tests for API endpoints
- Repository tests for data access
- Test coverage reporting
- Best practices for sustainable testing

**Remember:** Tests are documentation that never goes stale. Write them as you code, not after!

---

**Next:** [07-Epic6-Production-Ready.md](./07-Epic6-Production-Ready.md) - Prepare for deployment
