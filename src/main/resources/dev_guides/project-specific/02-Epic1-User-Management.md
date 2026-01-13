# 📦 Epic 1: Core User Management System

> **Duration:** 2-3 weeks  
> **Goal:** Build complete authentication and user management foundation

---

## Epic Overview

| Story | Title | Days | Status |
|-------|-------|------|--------|
| 1.1 | User Registration & Authentication | 5 | ⬜ |
| 1.2 | JWT Security Configuration | 4 | ⬜ |
| 1.3 | User Profile Management | 3 | ⬜ |
| 1.4 | User Preferences | 2 | ⬜ |

---

# Story 1.1: User Registration & Authentication

## User Story

**As a** new user  
**I want to** register an account and log in securely  
**So that** I can access my personal finance data

## Acceptance Criteria

- [ ] User can register with email, password, first name, last name
- [ ] Email must be unique
- [ ] Password encrypted with BCrypt
- [ ] User receives JWT token on successful login
- [ ] Token expires after 24 hours
- [ ] Default USER role assigned on registration

---

## Task 1.1.1: Create User Entity

### Why Entities First?

The entity defines **what data we store**. Everything else (DTOs, repositories, services) depends on this foundation.

### User.java

```java
package com.yourname.personalfinance.entity;

import com.yourname.personalfinance.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

/**
 * User entity representing a registered user.
 * 
 * Business Rules:
 * - Email is unique (database constraint)
 * - Password stored encrypted (never plaintext)
 * - New users get USER role by default
 * - active flag enables soft deletion
 * 
 * Indexes:
 * - email: Unique index for fast login lookups
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false, length = 255)
    private String password;  // Always BCrypt encrypted
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole userRole = UserRole.USER;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    private Integer age;
    
    // ========== Helper Methods ==========
    
    /**
     * Returns user's full name for display purposes.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Checks if user has admin privileges.
     */
    public boolean isAdmin() {
        return userRole == UserRole.ADMIN;
    }
}
```

### Key Patterns Explained

| Pattern | Why |
|---------|-----|
| `@Builder` | Fluent object creation in tests |
| `@Builder.Default` | Ensures defaults work with builder |
| `@Enumerated(STRING)` | Stores "USER" not "0" (survives reordering) |
| `@Index` | Fast email lookups during login |
| `unique = true` | Database-level uniqueness enforcement |

---

## Task 1.1.2: Create DTOs

### Why DTOs?

**Never expose entities to the API!**

```
Entity (internal)     →  DTO (API boundary)
- Has password        →  No password
- Has all fields      →  Only relevant fields
- Can change          →  API contract stable
```

### Request DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

/**
 * Registration request from client.
 * 
 * Validation:
 * - Email: Must be valid format
 * - Password: 8+ chars with complexity
 * - Names: Required, length limits
 */
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    String username,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 100, message = "Email must be at most 100 characters")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain: digit, lowercase, uppercase, special character"
    )
    String password,
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    String lastName,
    
    @Min(value = 13, message = "User must be at least 13 years old")
    @Max(value = 150, message = "Invalid age")
    Integer age
) {
    /**
     * Converts DTO to Entity.
     * Note: Password will be encrypted in service layer.
     */
    public User toEntity() {
        return User.builder()
            .username(username)
            .email(email)
            .password(password)  // Encrypted in service
            .firstName(firstName)
            .lastName(lastName)
            .age(age)
            .build();
    }
}
```

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

/**
 * Login request.
 */
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    String email,
    
    @NotBlank(message = "Password is required")
    String password
) {}
```

### Response DTOs

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.entity.enums.UserRole;
import java.time.LocalDateTime;

/**
 * User response (excludes sensitive data like password).
 */
public record UserResponse(
    Long id,
    String email,
    String username,
    String firstName,
    String lastName,
    String fullName,
    UserRole userRole,
    Integer age,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Factory method to create from entity.
     */
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
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.entity.enums.UserRole;

/**
 * Authentication response with JWT token.
 */
public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String email,
    String username,
    UserRole role,
    Long expiresIn
) {
    public static AuthResponse of(String token, User user, long expiresIn) {
        return new AuthResponse(
            token,
            "Bearer",
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getUserRole(),
            expiresIn
        );
    }
}
```

### Why Records for DTOs?

| Feature | Benefit |
|---------|---------|
| Immutable | Can't accidentally modify |
| Concise | No boilerplate |
| Built-in equals/hashCode | Correct comparisons |
| Built-in toString | Easy debugging |

---

## Task 1.1.3: Create UserRepository

```java
package com.yourname.personalfinance.repository;

import com.yourname.personalfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * 
 * Query Method Naming Convention:
 * - findBy{Field}: Returns Optional<Entity>
 * - existsBy{Field}: Returns boolean
 * - findAllBy{Field}: Returns List<Entity>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds user by email (used for login).
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds user by username.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Checks if email is already registered.
     */
    boolean existsByEmail(String email);
    
    /**
     * Checks if username is already taken.
     */
    boolean existsByUsername(String username);
    
    /**
     * Finds active users only.
     */
    List<User> findByActiveTrue();
    
    /**
     * Case-insensitive search by name.
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<User> searchByName(@Param("term") String term);
}
```

---

## Task 1.1.4: Create UserService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.CreateUserRequest;
import com.yourname.personalfinance.dto.response.UserResponse;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.exception.DuplicateResourceException;
import com.yourname.personalfinance.exception.ResourceNotFoundException;
import com.yourname.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for User operations.
 * 
 * Responsibilities:
 * - Business logic validation
 * - Password encryption
 * - DTO conversion
 * - Transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Registers a new user.
     * 
     * Business Rules:
     * 1. Email must be unique
     * 2. Username must be unique
     * 3. Password must be encrypted
     * 4. Default role is USER
     * 
     * @param request registration data
     * @return created user response
     * @throws DuplicateResourceException if email/username exists
     */
    @Transactional
    public UserResponse registerUser(CreateUserRequest request) {
        log.info("Registering new user with email: {}", request.email());
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                "Email already registered: " + request.email()
            );
        }
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(
                "Username already taken: " + request.username()
            );
        }
        
        // Create entity from DTO
        User user = request.toEntity();
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(request.password()));
        
        // Save and return
        User saved = userRepository.save(user);
        log.info("User registered successfully with id: {}", saved.getId());
        
        return UserResponse.from(saved);
    }
    
    /**
     * Gets user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with id: " + id
            ));
        return UserResponse.from(user);
    }
    
    /**
     * Gets user by email (for authentication).
     * Returns entity (not DTO) for internal use.
     */
    @Transactional(readOnly = true)
    public User getUserByEmailForAuth(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with email: " + email
            ));
    }
}
```

### Transaction Patterns

| Annotation | Use Case |
|------------|----------|
| `@Transactional` | Write operations (create, update, delete) |
| `@Transactional(readOnly = true)` | Read operations (Hibernate optimization) |

---

## Task 1.1.5: Create AuthService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.LoginRequest;
import com.yourname.personalfinance.dto.response.AuthResponse;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.exception.InvalidCredentialsException;
import com.yourname.personalfinance.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service handling login and JWT generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * Authenticates user and returns JWT token.
     * 
     * @param request login credentials
     * @return authentication response with token
     * @throws InvalidCredentialsException if credentials invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());
        
        // Find user
        User user;
        try {
            user = userService.getUserByEmailForAuth(request.email());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Invalid password for email: {}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        // Check if active
        if (!user.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }
        
        // Generate token
        String token = jwtUtil.generateToken(user);
        
        log.info("Login successful for user: {}", user.getId());
        return AuthResponse.of(token, user, jwtUtil.getExpiration());
    }
}
```

---

## Task 1.1.6: Create JwtUtil

```java
package com.yourname.personalfinance.security;

import com.yourname.personalfinance.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT utility for token generation and validation.
 * 
 * Token contains:
 * - Subject: User email
 * - Claims: userId, username, role
 * - Issued at: Creation timestamp
 * - Expiration: 24 hours after creation
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Getter
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * Generates JWT token for authenticated user.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getUserRole().name());
        
        return Jwts.builder()
            .claims(claims)
            .subject(user.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * Extracts email (subject) from token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    /**
     * Extracts user ID from token.
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    
    /**
     * Validates token (not expired, valid signature).
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

---

## Task 1.1.7: Create AuthController

```java
package com.yourname.personalfinance.controller;

import com.yourname.personalfinance.dto.request.CreateUserRequest;
import com.yourname.personalfinance.dto.request.LoginRequest;
import com.yourname.personalfinance.dto.response.AuthResponse;
import com.yourname.personalfinance.dto.response.UserResponse;
import com.yourname.personalfinance.service.AuthService;
import com.yourname.personalfinance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for registration and login.
 * 
 * Endpoints:
 * - POST /api/auth/register → Register new user
 * - POST /api/auth/login    → Login and get JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final AuthService authService;
    
    /**
     * Registers a new user.
     * 
     * @param request validated registration request
     * @return 201 CREATED with user response
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request) {
        
        log.info("POST /api/auth/register - email: {}", request.email());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Authenticates user and returns JWT token.
     * 
     * @param request login credentials
     * @return 200 OK with auth response including JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("POST /api/auth/login - email: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

---

## Task 1.1.8: Create Custom Exceptions

```java
package com.yourname.personalfinance.exception;

/**
 * Base exception for business logic errors.
 */
public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

```java
package com.yourname.personalfinance.exception;

/**
 * Thrown when a requested resource doesn't exist.
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
package com.yourname.personalfinance.exception;

/**
 * Thrown when attempting to create a duplicate resource.
 */
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

```java
package com.yourname.personalfinance.exception;

/**
 * Thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```

---

## Task 1.1.9: Write Unit Tests

### UserServiceTest.java

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.CreateUserRequest;
import com.yourname.personalfinance.dto.response.UserResponse;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.entity.enums.UserRole;
import com.yourname.personalfinance.exception.DuplicateResourceException;
import com.yourname.personalfinance.exception.ResourceNotFoundException;
import com.yourname.personalfinance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private CreateUserRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("johndoe")
            .email("john@example.com")
            .password("encrypted")
            .firstName("John")
            .lastName("Doe")
            .userRole(UserRole.USER)
            .active(true)
            .build();
        
        createRequest = new CreateUserRequest(
            "johndoe",
            "john@example.com",
            "Password123!",
            "John",
            "Doe",
            25
        );
    }
    
    @Test
    @DisplayName("registerUser - valid request - creates user successfully")
    void registerUser_ValidRequest_CreatesUser() {
        // Arrange
        when(userRepository.existsByEmail(createRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(createRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encrypted");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        UserResponse response = userService.registerUser(createRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.username()).isEqualTo("johndoe");
        
        verify(userRepository).existsByEmail(createRequest.email());
        verify(userRepository).existsByUsername(createRequest.username());
        verify(passwordEncoder).encode(createRequest.password());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("registerUser - duplicate email - throws DuplicateResourceException")
    void registerUser_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(createRequest.email())).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(createRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email already registered");
        
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("getUserById - existing user - returns UserResponse")
    void getUserById_ExistingUser_ReturnsResponse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act
        UserResponse response = userService.getUserById(1L);
        
        // Assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john@example.com");
    }
    
    @Test
    @DisplayName("getUserById - non-existent user - throws ResourceNotFoundException")
    void getUserById_NonExistentUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
```

---

## Story 1.1 Checklist

Before moving to Story 1.2:

- [ ] User entity created with all fields
- [ ] DTOs created (CreateUserRequest, LoginRequest, UserResponse, AuthResponse)
- [ ] UserRepository created with query methods
- [ ] UserService created with registration logic
- [ ] AuthService created with login logic
- [ ] JwtUtil created for token generation
- [ ] AuthController created with register/login endpoints
- [ ] Custom exceptions created
- [ ] Unit tests passing
- [ ] Can register via `POST /api/auth/register`
- [ ] Can login via `POST /api/auth/login`
- [ ] JWT token returned on login

---

# Story 1.2: JWT Security Configuration

## User Story

**As a** logged-in user  
**I want** my requests to be authenticated via JWT  
**So that** my data remains secure

## Acceptance Criteria

- [ ] All API endpoints (except /auth/*) require JWT
- [ ] JWT contains userId, email, role, expiration
- [ ] Invalid/expired tokens return 401 Unauthorized
- [ ] CORS configured for Angular frontend

---

## Task 1.2.1: Create UserDetailsImpl

```java
package com.yourname.personalfinance.security;

import com.yourname.personalfinance.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails implementation.
 * Wraps our User entity for the security framework.
 */
@RequiredArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {
    
    private final User user;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();  // We use email as username
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getActive();
    }
}
```

---

## Task 1.2.2: Create UserDetailsServiceImpl

```java
package com.yourname.personalfinance.security;

import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from database for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email
            ));
        
        return new UserDetailsImpl(user);
    }
}
```

---

## Task 1.2.3: Create JwtAuthenticationFilter

```java
package com.yourname.personalfinance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * 
 * Executes for every request:
 * 1. Extract JWT from Authorization header
 * 2. Validate token
 * 3. Load user details
 * 4. Set authentication in SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        // Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        
        try {
            // Extract email from token
            final String email = jwtUtil.extractEmail(jwt);
            
            // If email exists and not already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // Validate token
                if (jwtUtil.isTokenValid(jwt) && !jwtUtil.isTokenExpired(jwt)) {
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## Task 1.2.4: Update SecurityConfig

```java
package com.yourname.personalfinance.config;

import com.yourname.personalfinance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration with JWT authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT)
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session (no server-side session)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Story 1.2 Checklist

- [ ] UserDetailsImpl created
- [ ] UserDetailsServiceImpl created
- [ ] JwtAuthenticationFilter created
- [ ] SecurityConfig updated with JWT filter
- [ ] CORS configured for localhost:4200
- [ ] Public endpoints accessible without token
- [ ] Protected endpoints return 401 without token
- [ ] Protected endpoints work with valid token

---

# Story 1.3: User Profile Management

## User Story

**As a** logged-in user  
**I want to** view and update my profile  
**So that** I can keep my information current

## Acceptance Criteria

- [ ] User can view their own profile
- [ ] User can update first name, last name
- [ ] User cannot change email
- [ ] Admin can view all users
- [ ] User can soft-delete account

---

## Task 1.3.1: Create Update DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

/**
 * Request to update user profile.
 * Note: Email cannot be changed (business rule).
 */
public record UpdateUserRequest(
    @Size(max = 50, message = "First name must be at most 50 characters")
    String firstName,
    
    @Size(max = 50, message = "Last name must be at most 50 characters")
    String lastName,
    
    @Min(value = 13, message = "User must be at least 13 years old")
    @Max(value = 150, message = "Invalid age")
    Integer age
) {}
```

---

## Task 1.3.2: Add Methods to UserService

```java
// Add to UserService.java

/**
 * Updates user profile.
 * Only allows updating: firstName, lastName, age
 */
@Transactional
public UserResponse updateUser(Long userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User not found with id: " + userId
        ));
    
    // Update only provided fields (null = no change)
    if (request.firstName() != null) {
        user.setFirstName(request.firstName());
    }
    if (request.lastName() != null) {
        user.setLastName(request.lastName());
    }
    if (request.age() != null) {
        user.setAge(request.age());
    }
    
    // No explicit save needed - dirty checking handles it
    return UserResponse.from(user);
}

/**
 * Soft deletes user account.
 */
@Transactional
public void deactivateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User not found with id: " + userId
        ));
    
    user.setActive(false);
    log.info("User {} deactivated", userId);
}
```

---

## Task 1.3.3: Create UserController

```java
package com.yourname.personalfinance.controller;

import com.yourname.personalfinance.dto.request.UpdateUserRequest;
import com.yourname.personalfinance.dto.response.UserResponse;
import com.yourname.personalfinance.security.UserDetailsImpl;
import com.yourname.personalfinance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User profile management endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * Gets current user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("GET /api/users/me - userId: {}", userId);
        
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates current user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        
        Long userId = userDetails.getUser().getId();
        log.info("PUT /api/users/me - userId: {}", userId);
        
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deactivates current user's account.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("DELETE /api/users/me - userId: {}", userId);
        
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Gets all users (admin only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/users - admin request");
        // TODO: Implement with pagination in practice exercise
        return ResponseEntity.ok(List.of());
    }
}
```

---

## Story 1.3 Practice Exercise

Implement `getAllUsers` with pagination:

```java
/**
 * TODO: Implement getAllUsers with pagination
 * 
 * Requirements:
 * 1. Accept page, size, sortBy, direction parameters
 * 2. Return Page<UserResponse>
 * 3. Only active users
 * 4. Admin only access
 * 
 * Example: GET /api/users?page=0&size=20&sortBy=lastName&direction=ASC
 */
```

---

## Story 1.3 Checklist

- [ ] UpdateUserRequest DTO created
- [ ] updateUser method in UserService
- [ ] deactivateUser method in UserService
- [ ] UserController with /me endpoints
- [ ] @PreAuthorize for admin endpoints
- [ ] Unit tests for update/deactivate
- [ ] Integration tests for controller

---

# Story 1.4: User Preferences

## User Story

**As a** logged-in user  
**I want to** set my currency and language preferences  
**So that** the app displays data in my preferred format

## Acceptance Criteria

- [ ] Each user has one UserPreferences entity
- [ ] Default currency: USD, language: en
- [ ] Preferences created on user registration
- [ ] User can update preferences

---

## Task 1.4.1: Create UserPreferences Entity

```java
package com.yourname.personalfinance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User preferences (one-to-one with User).
 */
@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Column(nullable = false, length = 5)
    @Builder.Default
    private String language = "en";
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String theme = "light";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;
}
```

---

## Task 1.4.2: Update User Entity

```java
// Add to User.java

@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private UserPreferences preferences;
```

---

## Task 1.4.3: Create Preferences DTOs

```java
package com.yourname.personalfinance.dto.request;

import jakarta.validation.constraints.*;

public record UpdatePreferencesRequest(
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @Size(min = 2, max = 5, message = "Language must be 2-5 characters")
    String language,
    
    @Pattern(regexp = "^(light|dark)$", message = "Theme must be 'light' or 'dark'")
    String theme,
    
    Boolean emailNotifications
) {}
```

```java
package com.yourname.personalfinance.dto.response;

import com.yourname.personalfinance.entity.UserPreferences;

public record PreferencesResponse(
    Long id,
    String currency,
    String language,
    String theme,
    Boolean emailNotifications
) {
    public static PreferencesResponse from(UserPreferences prefs) {
        return new PreferencesResponse(
            prefs.getId(),
            prefs.getCurrency(),
            prefs.getLanguage(),
            prefs.getTheme(),
            prefs.getEmailNotifications()
        );
    }
}
```

---

## Task 1.4.4: Create UserPreferencesService

```java
package com.yourname.personalfinance.service;

import com.yourname.personalfinance.dto.request.UpdatePreferencesRequest;
import com.yourname.personalfinance.dto.response.PreferencesResponse;
import com.yourname.personalfinance.entity.User;
import com.yourname.personalfinance.entity.UserPreferences;
import com.yourname.personalfinance.exception.ResourceNotFoundException;
import com.yourname.personalfinance.repository.UserPreferencesRepository;
import com.yourname.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesService {
    
    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    
    /**
     * Creates default preferences for new user.
     */
    @Transactional
    public UserPreferences createDefaultPreferences(User user) {
        UserPreferences preferences = UserPreferences.builder()
            .user(user)
            .build();  // Uses @Builder.Default values
        
        return preferencesRepository.save(preferences);
    }
    
    /**
     * Gets preferences for user.
     */
    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences(Long userId) {
        UserPreferences prefs = preferencesRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Preferences not found for user: " + userId
            ));
        
        return PreferencesResponse.from(prefs);
    }
    
    /**
     * Updates user preferences.
     */
    @Transactional
    public PreferencesResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        UserPreferences prefs = preferencesRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Preferences not found for user: " + userId
            ));
        
        if (request.currency() != null) {
            prefs.setCurrency(request.currency());
        }
        if (request.language() != null) {
            prefs.setLanguage(request.language());
        }
        if (request.theme() != null) {
            prefs.setTheme(request.theme());
        }
        if (request.emailNotifications() != null) {
            prefs.setEmailNotifications(request.emailNotifications());
        }
        
        return PreferencesResponse.from(prefs);
    }
}
```

---

## Task 1.4.5: Update UserService Registration

```java
// Update registerUser in UserService.java

@Transactional
public UserResponse registerUser(CreateUserRequest request) {
    // ... existing validation code ...
    
    // Save user
    User saved = userRepository.save(user);
    
    // Create default preferences
    userPreferencesService.createDefaultPreferences(saved);
    
    log.info("User registered successfully with id: {}", saved.getId());
    return UserResponse.from(saved);
}
```

---

## Task 1.4.6: Add Preferences Endpoints to UserController

```java
// Add to UserController.java

private final UserPreferencesService preferencesService;

@GetMapping("/me/preferences")
public ResponseEntity<PreferencesResponse> getPreferences(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    Long userId = userDetails.getUser().getId();
    PreferencesResponse response = preferencesService.getPreferences(userId);
    return ResponseEntity.ok(response);
}

@PutMapping("/me/preferences")
public ResponseEntity<PreferencesResponse> updatePreferences(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody UpdatePreferencesRequest request) {
    
    Long userId = userDetails.getUser().getId();
    PreferencesResponse response = preferencesService.updatePreferences(userId, request);
    return ResponseEntity.ok(response);
}
```

---

## Story 1.4 Checklist

- [ ] UserPreferences entity created
- [ ] User entity updated with @OneToOne
- [ ] UserPreferencesRepository created
- [ ] UserPreferencesService created
- [ ] Preferences DTOs created
- [ ] Registration creates default preferences
- [ ] GET/PUT preferences endpoints work
- [ ] Unit tests for preferences service

---

## Epic 1 Complete Checklist

Before moving to Epic 2:

- [ ] **Story 1.1:** Registration and login working
- [ ] **Story 1.2:** JWT security protecting endpoints
- [ ] **Story 1.3:** Profile view/update/delete working
- [ ] **Story 1.4:** Preferences management working
- [ ] All unit tests passing
- [ ] Integration tests for auth flow
- [ ] Manual testing with Postman/curl

---

## Next Steps

→ Continue to [03-Epic2-Items-Categories.md](./03-Epic2-Items-Categories.md)
