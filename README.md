# Personal Finance & Price Tracking Application

A Spring Boot learning project for mastering enterprise Java development through building a real-world application.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 25 |
| Framework | Spring Boot 4 / Spring Framework 7 |
| Security | Spring Security 7 + JWT |
| Persistence | Hibernate 7 (JPA), Flyway |
| Database | MySQL 8 (primary), PostgreSQL (supported) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Docs | SpringDoc OpenAPI |
| Frontend | Angular 20 + Angular Material |

## Features

- **User Management** — Registration, authentication, profile & preferences
- **Item & Category Tracking** — Organize items with search & filtering
- **Shopping Lists** — Create lists, add items, mark as purchased
- **Price Tracking** — Price history, trends, and target price alerts

## Getting Started

### Prerequisites

- Java 25
- Maven 3.9+
- MySQL 8.x
- IDE with Lombok support

### Run

```bash
# Clone and navigate
git clone <repository-url>
cd personal-finance-app

# Configure database in application-mysql.yml, then:
mvn spring-boot:run
```

### API Documentation

Once running: `http://localhost:8888/swagger-ui.html`

## Project Structure

```
src/main/java/com/yourname/personalfinance/
├── config/          # Security, OpenAPI configuration
├── controller/      # REST endpoints
├── dto/             # Request/Response objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── repository/      # Data access layer
├── security/        # JWT filter, UserDetails implementation
└── service/         # Business logic
```

## Architecture

Vertical slice pattern — each feature flows through: `Entity → Repository → Service → Controller`

Key patterns:
- Constructor injection (no field `@Autowired`)
- DTOs for all API contracts (never expose entities)
- `BaseEntity` for audit fields (`createdAt`, `updatedAt`)
- Optimistic locking with `@Version`
- Global exception handling with consistent error responses

## Development Guides

Comprehensive guides available in `src/main/resources/dev_guides/`:

- `00-Master-Roadmap.md` — Full learning path
- `01-Project-Setup.md` — Environment configuration
- `02-07` — Feature implementation guides (Epics 1-6)
- `08-Patterns-Best-Practices.md` — Reusable patterns

## License

MIT
