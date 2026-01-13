# 🎯 Personal Finance App - Master Development Roadmap

> **Your Mentor's Promise:** This roadmap transforms you from an Associate to a confident Java/Spring Boot developer through hands-on, story-driven development.

---

## 📋 Quick Navigation

| Guide | Epic | Duration | Focus |
|-------|------|----------|-------|
| [01-Project-Setup](./01-Project-Setup.md) | Foundation | 1 day | Project scaffolding, dependencies, config |
| [02-Epic1-User-Management](./02-Epic1-User-Management.md) | Epic 1 | 2-3 weeks | Auth, JWT, User CRUD, Preferences |
| [03-Epic2-Items-Categories](./03-Epic2-Items-Categories.md) | Epic 2 | 2-3 weeks | Item & Category management |
| [04-Epic3-Shopping-Lists](./04-Epic3-Shopping-Lists.md) | Epic 3 | 2 weeks | Shopping list features |
| [05-Epic4-Price-Tracking](./05-Epic4-Price-Tracking.md) | Epic 4 | 2-3 weeks | Price history & alerts |
| [06-Epic5-Testing-Strategy](./06-Epic5-Testing-Strategy.md) | Epic 5 | Ongoing | Unit, Integration, API tests |
| [07-Epic6-Production-Ready](./07-Epic6-Production-Ready.md) | Epic 6 | 1-2 weeks | Docs, logging, migrations |
| [08-Patterns-Best-Practices](./08-Patterns-Best-Practices.md) | Reference | - | Reusable patterns & snippets |

---

## 🎓 Learning Philosophy

### The Vertical Slice Approach

Each story delivers **working functionality** from database to API:

```
Story 1.1: User Registration
├── Entity       → User.java (what we store)
├── DTOs         → CreateUserRequest, UserResponse (API contracts)
├── Repository   → UserRepository (data access)
├── Service      → UserService (business logic)
├── Controller   → AuthController (HTTP endpoints)
└── Tests        → Unit + Integration (quality assurance)
```

**Why this works:**
- ✅ Working features faster (demo anytime)
- ✅ Catch integration issues early
- ✅ Learn full stack per feature
- ✅ Real-world development pattern

### Teaching Pattern

Every concept follows:
1. **Explanation** - Why this matters
2. **Complete Example** - Working code you can run
3. **Practice Exercise** - You implement similar code
4. **Checklist** - Verify completion before moving on

---

## 🛠️ Tech Stack Reference

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Language (latest LTS features) |
| Spring Boot | 4.x | Application framework |
| Spring Framework | 7.x | Core framework |
| Spring Security | 7.x | Authentication & Authorization |
| Hibernate | 7.x | ORM / JPA implementation |
| MySQL | 8.x | Database |
| Lombok | Latest | Boilerplate reduction |
| jjwt | 0.12.x | JWT token handling |
| JUnit 5 | 5.x | Testing framework |
| Mockito | 5.x | Mocking framework |
| Flyway | 10.x | Database migrations |
| SpringDoc | 2.x | API documentation |

---

## 📦 Epic Overview

### Epic 1: Core User Management (Weeks 1-3)
Build the authentication foundation that every other feature depends on.

| Story | Description | Days |
|-------|-------------|------|
| 1.1 | User Registration & Authentication | 5 |
| 1.2 | JWT Security Configuration | 4 |
| 1.3 | User Profile Management | 3 |
| 1.4 | User Preferences | 2 |

**Deliverables:**
- [x] User can register with email/password
- [x] User can login and receive JWT
- [x] Protected endpoints require valid JWT
- [x] User can view/update profile
- [x] User can set preferences (currency, language)

---

### Epic 2: Item & Category Management (Weeks 4-6)
Core domain for tracking items users want to monitor.

| Story | Description | Days |
|-------|-------------|------|
| 2.1 | Category Management | 3 |
| 2.2 | Item Creation & Management | 4 |
| 2.3 | Item Search & Filtering | 3 |

**Deliverables:**
- [x] User can create/manage categories
- [x] User can create/manage items
- [x] Items linked to categories
- [x] Search by name, filter by category/priority
- [x] Pagination support

---

### Epic 3: Shopping List Management (Weeks 7-8)
Organize items into purchasable lists.

| Story | Description | Days |
|-------|-------------|------|
| 3.1 | Shopping List CRUD | 3 |
| 3.2 | Add Items to Shopping List | 4 |

**Deliverables:**
- [x] User can create multiple shopping lists
- [x] Add/remove items from lists
- [x] Mark items as purchased
- [x] List archival

---

### Epic 4: Price Tracking System (Weeks 9-10)
Monitor prices over time and alert on drops.

| Story | Description | Days |
|-------|-------------|------|
| 4.1 | Price History Tracking | 4 |
| 4.2 | Price Alerts & Comparisons | 4 |

**Deliverables:**
- [x] Add price entries for items
- [x] View price history with trends
- [x] Set target price alerts
- [x] Compare prices across stores

---

### Epic 5: Testing & Quality (Ongoing)
Build confidence through comprehensive testing.

| Story | Description | Days |
|-------|-------------|------|
| 5.1 | Unit Test Coverage | 5 |
| 5.2 | Integration Test Suite | 4 |
| 5.3 | Repository Tests | 3 |

**Deliverables:**
- [x] 80%+ test coverage
- [x] All services unit tested
- [x] All endpoints integration tested
- [x] Repository queries verified

---

### Epic 6: Production-Ready (Weeks 11-12)
Prepare for real-world deployment.

| Story | Description | Days |
|-------|-------------|------|
| 6.1 | API Documentation (OpenAPI) | 2 |
| 6.2 | Logging & Monitoring | 2 |
| 6.3 | Database Migrations (Flyway) | 2 |
| 6.4 | Global Exception Handling | 3 |

**Deliverables:**
- [x] Swagger UI available
- [x] Structured logging
- [x] Version-controlled schema
- [x] Consistent error responses

---

## 🗓️ Recommended Implementation Order

```
Week 1-2:   Story 1.1 → Story 1.2 (Auth foundation)
Week 3:     Story 1.3 → Story 1.4 (User features)
Week 4:     Story 2.1 (Categories)
Week 5:     Story 2.2 (Items)
Week 6:     Story 2.3 (Search/Filter)
Week 7:     Story 3.1 (Shopping Lists)
Week 8:     Story 3.2 (List Items)
Week 9:     Story 4.1 (Price History)
Week 10:    Story 4.2 (Price Alerts)
Week 11:    Story 6.1 → Story 6.2 (Docs & Logging)
Week 12:    Story 6.3 → Story 6.4 (Migrations & Errors)
```

**Testing:** Write tests as you build each story, not after!

---

## ✅ Definition of Done

Each story is complete when:

- [ ] All acceptance criteria met
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] Code follows project conventions
- [ ] No compiler warnings
- [ ] Self-reviewed for clean code
- [ ] Works with existing features (no regressions)

---

## 🚀 Getting Started

1. **Read** [01-Project-Setup.md](./01-Project-Setup.md) to scaffold your project
2. **Start** with [02-Epic1-User-Management.md](./02-Epic1-User-Management.md), Story 1.1
3. **Reference** [08-Patterns-Best-Practices.md](./08-Patterns-Best-Practices.md) for common patterns
4. **Test** continuously using [06-Epic5-Testing-Strategy.md](./06-Epic5-Testing-Strategy.md)

---

## 📝 Notes for Success

**Do:**
- Complete each story fully before starting the next
- Write tests as you code, not after
- Commit frequently with meaningful messages
- Ask "why" before copying code

**Don't:**
- Skip the practice exercises
- Return entities from controllers (use DTOs!)
- Forget to handle exceptions
- Use `ddl-auto: create` in production

---

**Let's build something great!** 🎉

Start with → [01-Project-Setup.md](./01-Project-Setup.md)
