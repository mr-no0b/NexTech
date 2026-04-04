# NexTech — Professional Tech Marketplace

A full-stack tech marketplace where **Sellers** list products, **Admins** approve them, and **Buyers** purchase them. Built as a complete Software Engineering Lab project demonstrating a production-grade development workflow.

---

## Table of Contents

- [Project Description](#project-description)
- [Architecture](#architecture)
- [ER Diagram](#er-diagram)
- [Tech Stack](#tech-stack)
- [Roles & Permissions](#roles--permissions)
- [API Endpoints](#api-endpoints)
- [Run Instructions](#run-instructions)
- [CI/CD Pipeline](#cicd-pipeline)
- [Docker](#docker)
- [Testing](#testing)
- [Demo Credentials](#demo-credentials)

---

## Project Description

NexTech is a tech-product marketplace where:
- **Sellers** register, submit products (laptops, RAM, processors, peripherals, etc.), and manage their listings.
- **Admins** review and approve/reject seller-submitted products before they appear publicly.
- **Buyers** browse the approved catalog, add items to cart, and place orders.

Key features: role-based access control, product approval workflow, cart & checkout, order history, admin dashboards, and a professional dark-themed UI.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Browser                        │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP (Thymeleaf Server-Side Rendering)
┌─────────────────────▼───────────────────────────────────────┐
│                    Spring Boot Application                    │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │  Controllers │  │   Services   │  │   Repositories    │  │
│  │              │→ │              │→ │  (Spring Data JPA)│  │
│  │ Home         │  │ UserService  │  │                   │  │
│  │ Auth         │  │ ProductSvc   │  │  UserRepo         │  │
│  │ Admin        │  │ CategorySvc  │  │  ProductRepo      │  │
│  │ Seller       │  │ CartService  │  │  OrderRepo        │  │
│  │ Buyer        │  │ OrderService │  │  CartRepo, etc.   │  │
│  │ Product      │  └──────────────┘  └──────────┬────────┘  │
│  └──────────────┘                               │            │
│                                                 │ JPA/Hibernate
│  ┌──────────────────────────────┐               │            │
│  │      Spring Security 6       │               │            │
│  │  BCrypt · URL Restrictions   │               │            │
│  │  @PreAuthorize · Role-based  │               │            │
│  └──────────────────────────────┘               │            │
└─────────────────────────────────────────────────┼───────────┘
                                                  │
                              ┌───────────────────▼──────────┐
                              │       PostgreSQL Database      │
                              │  users, roles, products,       │
                              │  categories, carts, cart_items,│
                              │  orders, order_items,          │
                              │  user_roles                    │
                              └──────────────────────────────┘
```

**Layered Architecture:**
```
Controller Layer  →  receives HTTP requests, validates input, calls services
Service Layer     →  business logic, transactions
Repository Layer  →  Spring Data JPA interfaces, DB queries
Entity Layer      →  JPA-mapped domain objects
DTO Layer         →  input validation (UserRegistrationDto, ProductDto, CheckoutDto)
Exception Layer   →  GlobalExceptionHandler (@ControllerAdvice) + ResourceNotFoundException
Config Layer      →  SecurityConfig, DataInitializer, CustomUserDetailsService
```

---

## ER Diagram

```
┌──────────┐     M:M via user_roles     ┌────────┐
│  users   │ ───────────────────────── │ roles  │
│──────────│                            │────────│
│ id (PK)  │                            │ id(PK) │
│ username │                            │ name   │
│ email    │                            └────────┘
│ password │
│ fullName │   1:M              1:M
│ phone    │◄──────── products ◄──── categories
│ shopName │          (seller_id FK)   │
│ address  │                           │ id, name, icon
│ enabled  │
│ createdAt│
└────┬─────┘
     │ 1:1          1:M
     ├──────── carts ──────────── cart_items
     │         (buyer_id FK)     (cart_id FK, product_id FK, quantity)
     │
     │ 1:M
     └──────── orders ─────────── order_items
               (buyer_id FK)     (order_id FK, product_id FK, qty, unitPrice)

Relationships:
  users   ──M:M──  roles         (via user_roles join table)
  users   ──1:M──  products      (as seller)
  users   ──1:1──  carts
  users   ──1:M──  orders
  categories ─1:M─ products
  products ──1:M── cart_items
  products ──1:M── order_items
  carts   ──1:M──  cart_items
  orders  ──1:M──  order_items
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Frontend | Thymeleaf + Bootstrap 5.3 + Font Awesome 6 |
| Database | PostgreSQL 16+ |
| ORM | Spring Data JPA / Hibernate 6 |
| Security | Spring Security 6 + BCrypt |
| Build Tool | Apache Maven 3.9 |
| Testing | JUnit 5 + Mockito + MockMvc |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Deployment | Render |

---

## Roles & Permissions

| Feature | Guest | Buyer | Seller | Admin |
|---|---|---|---|---|
| Browse products | ✅ | ✅ | ✅ | ✅ |
| View product detail | ✅ | ✅ | ✅ | ✅ |
| Register / Login | ✅ | — | — | — |
| Add to cart | ❌ | ✅ | ❌ | ❌ |
| Checkout / Place order | ❌ | ✅ | ❌ | ❌ |
| View own orders | ❌ | ✅ | ❌ | ❌ |
| Add/Edit/Delete own products | ❌ | ❌ | ✅ | ❌ |
| View seller dashboard | ❌ | ❌ | ✅ | ❌ |
| Approve / Reject products | ❌ | ❌ | ❌ | ✅ |
| Manage users (enable/disable) | ❌ | ❌ | ❌ | ✅ |
| Manage all orders (update status) | ❌ | ❌ | ❌ | ✅ |
| Admin dashboard | ❌ | ❌ | ❌ | ✅ |

---

## API Endpoints

### Public Routes
| Method | URL | Description |
|---|---|---|
| GET | `/` | Home page — featured products & categories |
| GET | `/products` | Browse all approved products (search + filter) |
| GET | `/product/{id}` | Product detail page |
| GET | `/login` | Login page |
| POST | `/login` | Authenticate user |
| GET | `/register` | Registration page |
| POST | `/register` | Create new Buyer or Seller account |

### Buyer Routes (`ROLE_BUYER`)
| Method | URL | Description |
|---|---|---|
| GET | `/buyer/dashboard` | Buyer dashboard |
| GET | `/cart` | View shopping cart |
| POST | `/cart/add` | Add product to cart |
| POST | `/cart/update` | Update cart item quantity |
| POST | `/cart/remove` | Remove item from cart |
| POST | `/cart/checkout` | Place order from cart |
| GET | `/buyer/orders` | Order history |
| GET | `/buyer/orders/{id}` | Order detail |

### Seller Routes (`ROLE_SELLER`)
| Method | URL | Description |
|---|---|---|
| GET | `/seller/dashboard` | Seller dashboard |
| GET | `/seller/products` | Manage own products |
| GET | `/seller/products/add` | Add product form |
| POST | `/seller/products/add` | Submit new product |
| GET | `/seller/products/{id}/edit` | Edit product form |
| POST | `/seller/products/{id}/edit` | Update product |
| POST | `/seller/products/{id}/delete` | Delete own product |

### Admin Routes (`ROLE_ADMIN`)
| Method | URL | Description |
|---|---|---|
| GET | `/admin/dashboard` | Admin dashboard |
| GET | `/admin/products` | All products management |
| POST | `/admin/products/{id}/approve` | Approve product |
| POST | `/admin/products/{id}/reject` | Reject product with reason |
| POST | `/admin/products/{id}/delete` | Delete any product |
| GET | `/admin/users` | User management |
| POST | `/admin/users/{id}/toggle` | Enable/disable user account |
| GET | `/admin/orders` | All orders management |
| POST | `/admin/orders/{id}/status` | Update order status |

---

## Run Instructions

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 16+
- Docker & Docker Compose (for containerized run)

### Option 1 — Run Locally (bare metal)

**1. Create the database:**
```bash
sudo -u postgres psql -c "CREATE DATABASE nextech_db;"
sudo -u postgres psql -c "CREATE USER nextech WITH PASSWORD 'nextech123';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE nextech_db TO nextech;"
sudo -u postgres psql -d nextech_db -c "GRANT ALL ON SCHEMA public TO nextech;"
```

**2. Run the application:**
```bash
cd NexTech
mvn spring-boot:run
```

**3. Open:** http://localhost:8080

---

### Option 2 — Run with Docker Compose

```bash
cd NexTech
docker compose up --build
```

The app and PostgreSQL start automatically. Open http://localhost:8080.

**Custom credentials (optional):**
```bash
DB_NAME=mydb DB_USER=myuser DB_PASSWORD=mypass docker compose up --build
```

---

### Option 3 — Build JAR and run

```bash
mvn package -DskipTests
java -jar target/nextech-1.0.0.jar
```

With environment variables (for production):
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/nextech_db \
SPRING_DATASOURCE_USERNAME=nextech \
SPRING_DATASOURCE_PASSWORD=secret \
java -jar target/nextech-1.0.0.jar
```

---

## Demo Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Seller | `techseller` | `seller123` |
| Buyer | `buyer1` | `buyer123` |

After login, each role is redirected to its own dashboard automatically.

---

## Testing

```bash
mvn test
```

**Test breakdown (18 total):**

| Test Class | Tests | Type |
|---|---|---|
| `UserServiceTest` | 5 | Unit |
| `ProductServiceTest` | 5 | Unit |
| `OrderServiceTest` | 5 | Unit |
| `HomeControllerTest` | 1 | Integration |
| `AuthControllerTest` | 2 | Integration |

Tests use: JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`), `@WebMvcTest` with `MockMvc`.

---

## CI/CD Pipeline

The GitHub Actions pipeline (`.github/workflows/ci-cd.yml`) runs on every push to `main` or `develop`, and on pull requests targeting `main`.

```
Push to main / PR to main
         │
         ▼
┌─────────────────────┐
│  build-and-test job  │  ← Spins up PostgreSQL service container
│  • mvn verify        │  ← Compiles + runs all 18 tests
│  • upload test report│
└──────────┬──────────┘
           │ (only on main branch)
           ▼
┌─────────────────────┐
│  docker-build job    │  ← Builds Docker image
│  • docker buildx     │  ← Pushes to Docker Hub
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  deploy job          │  ← Triggers Render deploy hook
│  • curl POST hook    │  ← Auto-deploys the new image
└─────────────────────┘
```

**Secrets required in GitHub repository settings:**
| Secret | Purpose |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub account username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `RENDER_DEPLOY_HOOK_URL` | Render service deploy hook URL |

**Branch strategy:**
- `main` — production-ready, **branch protection enabled** (requires PR + 1 review approval, no direct push)
- `develop` — integration branch for completed features
- `feature/*` — individual feature branches, merged into `develop` via PR

---

## Docker

The project uses a **multi-stage Dockerfile**:
- Stage 1 (`builder`): Maven + JDK 17 — compiles and packages the JAR
- Stage 2 (`runtime`): Eclipse Temurin JRE 17 — minimal runtime image

`docker-compose.yml` defines two services:
- `postgres` — PostgreSQL 16 with health check
- `app` — Spring Boot app, starts only after postgres is healthy

No credentials are hardcoded — all values use environment variables with safe defaults.

---

## Project Structure

```
NexTech/
├── src/
│   ├── main/
│   │   ├── java/com/nextech/
│   │   │   ├── config/          # SecurityConfig, DataInitializer, CustomUserDetailsService
│   │   │   ├── controller/      # 6 controllers (Home, Auth, Admin, Seller, Buyer, Product)
│   │   │   ├── dto/             # UserRegistrationDto, ProductDto, CheckoutDto
│   │   │   ├── entity/          # 11 entities/enums (User, Product, Order, Cart, etc.)
│   │   │   ├── exception/       # GlobalExceptionHandler, ResourceNotFoundException
│   │   │   ├── repository/      # 8 Spring Data JPA repositories
│   │   │   ├── service/         # 5 service interfaces + 5 implementations
│   │   │   └── NexTechApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/css/style.css
│   │       └── templates/       # 18 Thymeleaf templates
│   └── test/
│       └── java/com/nextech/
│           ├── controller/      # HomeControllerTest, AuthControllerTest
│           └── service/         # UserServiceTest, ProductServiceTest, OrderServiceTest
├── .github/workflows/ci-cd.yml
├── Dockerfile
├── docker-compose.yml
├── .gitignore
└── pom.xml
```
