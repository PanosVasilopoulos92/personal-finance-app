# 🏗️ Project Setup Guide

> **Goal:** Get your development environment ready and project scaffolded with all dependencies configured.

---

## Prerequisites

Before starting, ensure you have:

- [ ] **Java 25** - `java --version` should show 25.x
- [ ] **Maven 3.9+** - `mvn --version`
- [ ] **MySQL 8.x** - `mysql --version`
- [ ] **IDE** - IntelliJ IDEA recommended (enable Lombok plugin)
- [ ] **Git** - `git --version`

---

## Step 1: Create Project Structure

### Using Spring Initializr

Visit [start.spring.io](https://start.spring.io) with these settings:

| Setting | Value |
|---------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.0.x (latest) |
| Group | com.yourname |
| Artifact | personal-finance-app |
| Packaging | Jar |
| Java | 25 |

**Dependencies to add:**
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- MySQL Driver
- Lombok
- Spring Boot DevTools

---

## Step 2: Complete pom.xml

Replace the generated `pom.xml` with this comprehensive version:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.yourname</groupId>
    <artifactId>personal-finance-app</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>personal-finance-app</name>
    <description>Personal Finance and Price Tracking Application</description>
    
    <properties>
        <java.version>25</java.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        <!-- Development -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- JaCoCo for test coverage -->
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
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Step 3: Project Structure

Create this folder structure:

```
src/
├── main/
│   ├── java/com/yourname/personalfinance/
│   │   ├── PersonalFinanceApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── OpenApiConfig.java
│   │   ├── entity/
│   │   │   ├── BaseEntity.java
│   │   │   ├── User.java
│   │   │   └── enums/
│   │   │       └── UserRole.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── UserDetailsImpl.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── ResourceNotFoundException.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       ├── application-prod.yml
│       └── db/migration/
└── test/
    └── java/com/yourname/personalfinance/
        ├── repository/
        ├── service/
        └── controller/
```

---

## Step 4: Base Configuration

### application.yml (Base)

```yaml
spring:
  application:
    name: personal-finance-app
    
  jpa:
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
        
  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null
    time-zone: UTC

server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always

jwt:
  expiration: 86400000  # 24 hours

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_finance_dev?createDatabaseIfNotExist=true&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

jwt:
  secret: dev-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-this-is-only-for-development

logging:
  level:
    com.yourname.personalfinance: DEBUG
    org.hibernate.SQL: DEBUG
```

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    
  flyway:
    enabled: false

jwt:
  secret: test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-testing-only
  expiration: 3600000  # 1 hour for faster tests

logging:
  level:
    root: ERROR
    com.yourname.personalfinance: INFO
```

---

## Step 5: BaseEntity

Every entity will extend this for consistent audit fields:

```java
package com.yourname.personalfinance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Base entity providing common fields for all entities.
 * 
 * Fields:
 * - id: Auto-generated primary key
 * - createdAt: Timestamp when entity was created (immutable)
 * - updatedAt: Timestamp when entity was last modified
 * 
 * Usage: All entities should extend this class.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Why MappedSuperclass?**
- Provides common fields without a separate table
- `@CreationTimestamp` auto-sets on insert
- `@UpdateTimestamp` auto-updates on every change
- `updatable = false` prevents accidental modification

---

## Step 6: UserRole Enum

```java
package com.yourname.personalfinance.entity.enums;

/**
 * User roles for authorization.
 * 
 * USER: Standard user access
 * ADMIN: Administrative access (manage all users, system settings)
 */
public enum UserRole {
    USER,
    ADMIN
}
```

**Why Enum?**
- Type-safe (compiler catches typos)
- Self-documenting
- Survives refactoring
- `@Enumerated(EnumType.STRING)` stores "USER" not "0"

---

## Step 7: Initial Security Config (Disabled for Now)

Create a temporary security config that permits all requests. We'll build proper security in Story 1.2.

```java
package com.yourname.personalfinance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary security configuration.
 * 
 * IMPORTANT: This permits all requests for initial development.
 * Replace with proper JWT security in Story 1.2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // TODO: Secure in Story 1.2
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Step 8: Database Setup

### Create MySQL Database

```sql
-- Run in MySQL client
CREATE DATABASE personal_finance_dev;
CREATE DATABASE personal_finance_test;

-- Create user (optional, for better security)
CREATE USER 'finance_app'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON personal_finance_dev.* TO 'finance_app'@'localhost';
GRANT ALL PRIVILEGES ON personal_finance_test.* TO 'finance_app'@'localhost';
FLUSH PRIVILEGES;
```

---

## Step 9: Verify Setup

### Run the Application

```bash
# From project root
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Expected output:**
```
Started PersonalFinanceApplication in X.XXX seconds
```

### Verify Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

---

## Project Setup Checklist

Before starting Epic 1:

- [ ] Java 25 installed and configured
- [ ] Maven builds successfully (`mvn clean install`)
- [ ] MySQL database created
- [ ] Application starts without errors
- [ ] Health endpoint returns UP
- [ ] IDE recognizes Lombok annotations
- [ ] Project structure created
- [ ] Base configuration files in place
- [ ] BaseEntity created
- [ ] UserRole enum created

---

## Common Issues

### "Cannot resolve symbol" for Lombok

**Solution:** Install Lombok plugin in IDE and enable annotation processing:
- IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable

### "Access denied for user"

**Solution:** Check MySQL credentials in `application-dev.yml` match your setup.

### "Unknown database"

**Solution:** Create the database manually:
```sql
CREATE DATABASE personal_finance_dev;
```

---

## Next Steps

Your project is ready! Continue to:

→ [02-Epic1-User-Management.md](./02-Epic1-User-Management.md) - Story 1.1: User Registration
