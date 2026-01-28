# 🚀 Epic 6: Production-Ready Features

> **Duration:** 1-2 weeks | **Stories:** 6.1, 6.2, 6.3, 6.4  
> **Goal:** Transform your application from "works on my machine" to "ready for production"

---

## 📋 Epic Overview

Production readiness means your application is:
- **Documented** - API docs for frontend developers
- **Observable** - Logging and monitoring for debugging
- **Maintainable** - Database migrations for safe deployments
- **Resilient** - Proper error handling for users

---

## Story 6.1: API Documentation with OpenAPI

**As a** frontend developer  
**I want** interactive API documentation  
**So that** I know how to consume the API

### Step 1: Add Dependencies

```xml
<!-- SpringDoc OpenAPI (Swagger UI) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

---

### Step 2: Configure OpenAPI

Create `OpenApiConfig.java`:

```java
/**
 * OpenAPI configuration for API documentation.
 * 
 * Swagger UI available at: /swagger-ui.html
 * OpenAPI spec available at: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Personal Finance API")
                .version("1.0.0")
                .description("""
                    REST API for the Personal Finance & Price Tracking Application.
                    
                    ## Authentication
                    Most endpoints require JWT authentication. 
                    Include the token in the Authorization header:
                    `Authorization: Bearer <your-token>`
                    
                    ## Rate Limiting
                    API requests are limited to 100 requests per minute.
                    """)
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                .description("GitHub Repository")
                .url("https://github.com/yourrepo"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtained from /api/auth/login")));
    }
}
```

---

### Step 3: Document Controllers

```java
/**
 * REST API Controller for User management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {
    
    private final UserService userService;
    
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their unique identifier. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "id": 1,
                        "email": "john@example.com",
                        "username": "johndoe",
                        "firstName": "John",
                        "lastName": "Doe",
                        "role": "USER"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        
        log.info("GET /api/users/{}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Create new user",
        description = "Registers a new user account. Email must be unique."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            headers = @Header(
                name = "Location",
                description = "URL of created user",
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists"
        )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration data",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = CreateUserRequest.class),
                    examples = @ExampleObject(value = """
                        {
                            "username": "johndoe",
                            "email": "john@example.com",
                            "firstName": "John",
                            "lastName": "Doe",
                            "password": "SecurePass123!",
                            "age": 25
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateUserRequest request) {
        
        log.info("POST /api/users - Creating user: {}", request.email());
        
        UserResponse response = userService.registerUser(request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }
    
    @Operation(
        summary = "Get all users (paginated)",
        description = "Returns a paginated list of all users. Admin only."
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field", example = "lastName")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, direction);
        return ResponseEntity.ok(users);
    }
}
```

---

### Step 4: Document DTOs

```java
/**
 * Response containing user details.
 */
@Schema(description = "User information response")
public record UserResponse(
    @Schema(description = "Unique identifier", example = "1")
    Long id,
    
    @Schema(description = "Email address", example = "john@example.com")
    String email,
    
    @Schema(description = "Username", example = "johndoe")
    String username,
    
    @Schema(description = "First name", example = "John")
    String firstName,
    
    @Schema(description = "Last name", example = "Doe")
    String lastName,
    
    @Schema(description = "Full name", example = "John Doe")
    String fullName,
    
    @Schema(description = "User role", example = "USER")
    UserRolesEnum role,
    
    @Schema(description = "User age", example = "25")
    Integer age,
    
    @Schema(description = "Account creation timestamp")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
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
            user.getAge(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

/**
 * Request to create a new user.
 */
@Schema(description = "User registration request")
public record CreateUserRequest(
    @Schema(description = "Username (3-50 characters)", example = "johndoe", minLength = 3, maxLength = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    String username,
    
    @Schema(description = "Email address", example = "john@example.com", format = "email")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    String email,
    
    @Schema(description = "First name", example = "John")
    String firstName,
    
    @Schema(description = "Last name", example = "Doe")
    String lastName,
    
    @Schema(description = "Password (8+ chars, must include digit, uppercase, lowercase, special)", 
            example = "SecurePass123!", minLength = 8)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    String password,
    
    @Schema(description = "User age (must be 13+)", example = "25", minimum = "13")
    Integer age
) {}
```

---

### Step 5: Configure Swagger UI

Add to `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
    filter: true
  default-produces-media-type: application/json
```

Access documentation at:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

### Story 6.1 Checklist

- [ ] springdoc-openapi dependency added
- [ ] OpenApiConfig with security scheme
- [ ] @Tag on all controllers
- [ ] @Operation on all endpoints
- [ ] @ApiResponses with all possible responses
- [ ] @Schema on all DTOs
- [ ] Example values provided
- [ ] Swagger UI accessible and working

---

## Story 6.2: Logging & Monitoring

**As a** DevOps engineer  
**I want** structured logging  
**So that** I can monitor and debug the application

### Step 1: Configure Logback

Create `src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Include Spring Boot defaults -->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- Properties -->
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"
                    defaultValue="personal-finance-app"/>
    <property name="LOG_PATH" value="${LOG_PATH:-logs}"/>

    <!-- Console appender for development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight(%-5level) [%thread] %cyan(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- JSON appender for production -->
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/application-json.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application-json.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app":"${APP_NAME}"}</customFields>
        </encoder>
    </appender>

    <!-- Plain text file appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Profile-specific configurations -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="org.viators.personalfinanceapp" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
    </springProfile>

    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="FILE"/>
            <appender-ref ref="JSON_FILE"/>
        </root>
        <logger name="org.viators.personalfinanceapp" level="INFO"/>
    </springProfile>

</configuration>
```

---

### Step 2: Add Logging to Services

```java
@Service
@RequiredArgsConstructor
@Slf4j  // Lombok annotation - creates 'log' field
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse registerUser(CreateUserRequest request) {
        // Log at INFO level for important business events
        log.info("Registering new user with email: {}", request.email());
        
        // Check for duplicate
        if (userRepository.existsByEmail(request.email())) {
            // Log at WARN for expected but notable situations
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new DuplicateResourceException("Email already registered");
        }
        
        try {
            User user = createUser(request);
            User savedUser = userRepository.save(user);
            
            // Log success with relevant details
            log.info("User registered successfully: id={}, email={}", 
                savedUser.getId(), savedUser.getEmail());
            
            return UserResponse.from(savedUser);
            
        } catch (Exception e) {
            // Log at ERROR for unexpected problems
            log.error("Failed to register user: {}", request.email(), e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        // Log at DEBUG for routine operations
        log.debug("Fetching user with id: {}", id);
        
        return userRepository.findById(id)
            .map(UserResponse::from)
            .orElseThrow(() -> {
                log.debug("User not found with id: {}", id);
                return new ResourceNotFoundException("User not found: " + id);
            });
    }
}
```

---

### Step 3: Create Request Logging Filter

```java
/**
 * Filter that logs all HTTP requests and responses.
 * Useful for debugging and monitoring.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Add request ID to MDC for tracing
        MDC.put("requestId", requestId);
        
        try {
            // Log request
            log.info("→ {} {} (requestId={})", 
                request.getMethod(), 
                request.getRequestURI(),
                requestId);
            
            // Process request
            filterChain.doFilter(request, response);
            
            // Log response
            long duration = System.currentTimeMillis() - startTime;
            log.info("← {} {} {} ({}ms, requestId={})",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                requestId);
                
        } finally {
            MDC.remove("requestId");
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't log static resources or actuator
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger") || 
               path.startsWith("/v3/api-docs");
    }
}
```

---

### Step 4: Configure Actuator

Add to `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  info:
    env:
      enabled: true
    java:
      enabled: true
    os:
      enabled: true

# Application info
info:
  app:
    name: Personal Finance App
    version: 1.0.0
    description: REST API for personal finance management
```

Available endpoints:
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics list
- `GET /actuator/metrics/{name}` - Specific metric
- `GET /actuator/loggers` - Logger levels
- `POST /actuator/loggers/{name}` - Change log level

---

### Story 6.2 Checklist

- [ ] Logback configuration created
- [ ] @Slf4j added to all classes
- [ ] INFO level for business events
- [ ] WARN level for notable situations
- [ ] ERROR level for unexpected problems
- [ ] DEBUG level for routine operations
- [ ] Request logging filter implemented
- [ ] MDC for request tracing
- [ ] Actuator endpoints configured
- [ ] Health checks working

---

## Story 6.3: Database Migrations with Flyway

**As a** developer  
**I want** version-controlled database schema  
**So that** deployments are repeatable and safe

### Step 1: Add Dependencies

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

### Step 2: Configure Flyway

Add to `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
    
  jpa:
    hibernate:
      ddl-auto: validate  # Let Flyway manage schema!
```

---

### Step 3: Create Migration Files

Create directory: `src/main/resources/db/migration/`

**V1__create_users_table.sql:**

```sql
-- V1: Create users table
-- Author: Your Name
-- Date: 2024-01-01

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    age INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_email (email),
    INDEX idx_user_username (username),
    INDEX idx_user_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V2__create_user_preferences_table.sql:**

```sql
-- V2: Create user preferences table
-- Author: Your Name
-- Date: 2024-01-02

CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_preferences_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_preferences_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V3__create_categories_table.sql:**

```sql
-- V3: Create categories table
-- Author: Your Name
-- Date: 2024-01-03

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(50),
    color VARCHAR(7),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_category_user_name (user_id, name),
    INDEX idx_category_user (user_id),
    INDEX idx_category_archived (archived)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V4__create_items_table.sql:**

```sql
-- V4: Create items table
-- Author: Your Name
-- Date: 2024-01-04

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    url VARCHAR(500),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    target_price DECIMAL(10,2),
    alert_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_item_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_item_user_name (user_id, name),
    INDEX idx_item_user (user_id),
    INDEX idx_item_category (category_id),
    INDEX idx_item_priority (priority),
    INDEX idx_item_favorite (favorite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V5__create_stores_table.sql:**

```sql
-- V5: Create stores table
-- Author: Your Name
-- Date: 2024-01-05

CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    website VARCHAR(500),
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_store_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_store_name (name),
    INDEX idx_store_user (user_id),
    INDEX idx_store_global (is_global)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert some global stores
INSERT INTO stores (name, is_global, active) VALUES
    ('Amazon', TRUE, TRUE),
    ('Walmart', TRUE, TRUE),
    ('Target', TRUE, TRUE),
    ('Costco', TRUE, TRUE),
    ('Best Buy', TRUE, TRUE);
```

**V6__create_price_entries_table.sql:**

```sql
-- V6: Create price entries table
-- Author: Your Name
-- Date: 2024-01-06

CREATE TABLE price_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    observed_at TIMESTAMP NOT NULL,
    notes VARCHAR(500),
    is_sale BOOLEAN NOT NULL DEFAULT FALSE,
    original_price DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_price_item FOREIGN KEY (item_id) 
        REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_price_store FOREIGN KEY (store_id) 
        REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_price_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_price_item (item_id),
    INDEX idx_price_store (store_id),
    INDEX idx_price_date (observed_at),
    INDEX idx_price_item_date (item_id, observed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### Step 4: Migration Commands

```bash
# Check migration status
mvn flyway:info

# Run pending migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Clean database (DANGER - drops all objects!)
mvn flyway:clean

# Repair failed migrations
mvn flyway:repair
```

---

### Story 6.3 Checklist

- [ ] Flyway dependencies added
- [ ] ddl-auto set to validate
- [ ] Migration files created with proper naming
- [ ] All tables have proper indexes
- [ ] Foreign key constraints defined
- [ ] Migrations tested on clean database
- [ ] flyway:info shows correct state

---

## Story 6.4: Exception Handling & Validation

**As a** user  
**I want** clear error messages  
**So that** I understand what went wrong

### Step 1: Create Custom Exceptions

```java
/**
 * Base exception for all business exceptions.
 */
public abstract class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * Thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id), "RESOURCE_NOT_FOUND");
    }
}

/**
 * Thrown when attempting to create a duplicate resource.
 */
public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}

/**
 * Thrown when business validation fails.
 */
public class BusinessValidationException extends BusinessException {
    
    public BusinessValidationException(String message) {
        super(message, "VALIDATION_FAILED");
    }
}

/**
 * Thrown when password validation fails.
 */
public class InvalidPasswordException extends BusinessException {
    
    public InvalidPasswordException(String message) {
        super(message, "INVALID_PASSWORD");
    }
}

/**
 * Thrown when user tries to access unauthorized resource.
 */
public class AccessDeniedException extends BusinessException {
    
    public AccessDeniedException(String message) {
        super(message, "ACCESS_DENIED");
    }
}
```

---

### Step 2: Create Error Response DTOs

```java
/**
 * Standard error response for API errors.
 */
@Schema(description = "Error response")
public record ErrorResponse(
    @Schema(description = "HTTP status code", example = "404")
    int status,
    
    @Schema(description = "Error code for frontend", example = "RESOURCE_NOT_FOUND")
    String errorCode,
    
    @Schema(description = "Human-readable error message")
    String message,
    
    @Schema(description = "Request path that caused the error")
    String path,
    
    @Schema(description = "Timestamp when error occurred")
    LocalDateTime timestamp
) {
    public static ErrorResponse of(HttpStatus status, String errorCode, String message, String path) {
        return new ErrorResponse(
            status.value(),
            errorCode,
            message,
            path,
            LocalDateTime.now()
        );
    }
}

/**
 * Error response with field-level validation errors.
 */
@Schema(description = "Validation error response")
public record ValidationErrorResponse(
    @Schema(description = "HTTP status code", example = "400")
    int status,
    
    @Schema(description = "Error code", example = "VALIDATION_FAILED")
    String errorCode,
    
    @Schema(description = "General error message")
    String message,
    
    @Schema(description = "Field-specific errors")
    Map<String, String> fieldErrors,
    
    @Schema(description = "Request path")
    String path,
    
    @Schema(description = "Timestamp")
    LocalDateTime timestamp
) {
    public static ValidationErrorResponse of(String message, Map<String, String> fieldErrors, String path) {
        return new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            message,
            fieldErrors,
            path,
            LocalDateTime.now()
        );
    }
}
```

---

### Step 3: Create Global Exception Handler

```java
/**
 * Global exception handler for all controllers.
 * 
 * Catches exceptions and converts them to standardized error responses.
 * Logs errors appropriately based on severity.
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    
    // ==================== BUSINESS EXCEPTIONS ====================
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        log.debug("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.NOT_FOUND,
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        
        log.warn("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.CONFLICT,
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler({BusinessValidationException.class, InvalidPasswordException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidation(
            BusinessException ex,
            HttpServletRequest request) {
        
        log.warn("Business validation failed: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.FORBIDDEN,
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    // ==================== VALIDATION EXCEPTIONS ====================
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.debug("Validation failed: {} errors", ex.getErrorCount());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null 
                    ? error.getDefaultMessage() 
                    : "Invalid value",
                (existing, replacement) -> existing  // Keep first if duplicates
            ));
        
        ValidationErrorResponse error = ValidationErrorResponse.of(
            "Validation failed",
            fieldErrors,
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        Map<String, String> fieldErrors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing
            ));
        
        ValidationErrorResponse error = ValidationErrorResponse.of(
            "Constraint violation",
            fieldErrors,
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // ==================== SECURITY EXCEPTIONS ====================
    
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSecurityAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.warn("Security access denied for path: {}", request.getRequestURI());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "You don't have permission to access this resource",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {
        
        log.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "Authentication failed",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    // ==================== CATCH-ALL ====================
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        // Log full stack trace for unexpected errors
        log.error("Unexpected error occurred at {}: {}", 
            request.getRequestURI(), ex.getMessage(), ex);
        
        // Don't expose internal details to client!
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### Story 6.4 Checklist

- [ ] Custom exception hierarchy created
- [ ] ErrorResponse DTO with error codes
- [ ] ValidationErrorResponse with field errors
- [ ] GlobalExceptionHandler with all handlers
- [ ] Proper logging in exception handlers
- [ ] No internal details exposed to clients
- [ ] Consistent error response format
- [ ] Error codes for frontend handling

---

## 🎯 Epic 6 Complete!

Your application now has:

- **API Documentation** - Interactive Swagger UI
- **Logging** - Structured logs for debugging
- **Migrations** - Version-controlled schema changes
- **Error Handling** - Clear, consistent error responses

### Production Checklist

Before deploying:
- [ ] All environment variables documented
- [ ] Swagger UI secured in production
- [ ] Log levels appropriate for production
- [ ] All migrations tested
- [ ] Error messages don't expose internals
- [ ] Health checks accessible
- [ ] Metrics collection enabled

---

**Congratulations!** 🎉 Your Personal Finance App is now production-ready!

---

**Next:** [08-Patterns-Best-Practices.md](./08-Patterns-Best-Practices.md) - Reference guide for common patterns
