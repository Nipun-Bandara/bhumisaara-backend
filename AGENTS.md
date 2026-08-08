# Agent Guidelines & Repository Documentation (AGENTS.md)

Welcome to the **Bhumisaara Backend** repository. This document serves as the primary technical specification, architecture guide, and operational manual for AI agents and developers working on this Spring Boot application.

---

## 1. Project Overview

**Bhumisaara Backend** is a Java Spring Boot Web2.5 supply chain application designed for agricultural and fertilizer tracking (including blockchain tokenization proof). It integrates standard RESTful APIs with PostgreSQL database persistence and JWT-based authentication.

### Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.4.x / 4.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`)
- **Database**: PostgreSQL (with MySQL runtime driver support)
- **ORM / Persistence**: Hibernate / Spring Data JPA
- **Security**: Spring Security + JSON Web Tokens (JJWT 0.11.5)
- **Environment Management**: `me.paulschwarz:spring-dotenv` (loads `.env.development`, `.env.staging`, `.env.production`)
- **Boilerplate Reduction**: Lombok (`@Data`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **Build Tool**: Apache Maven (`./mvnw`)

---

## 2. Directory & Package Structure

```
src/main/java/com/bandits/bhumisaara/
├── BhumisaaraApplication.java      # Main application entry point
├── controller/                     # REST API Controllers
│   ├── AuthController.java         # Authentication & Token validation endpoints
│   └── FertilizerBatchController.java # Fertilizer batch minting & querying endpoints
├── dto/                            # Data Transfer Objects
│   ├── request/                    # Incoming payload DTOs with validation
│   │   ├── BatchRequestDTO.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── RegisterRequest.java
│   └── response/                   # Outgoing API response DTOs
│       ├── AuthResponse.java
│       ├── BatchResponseDTO.java
│       ├── ErrorResponse.java
│       └── TokenValidationResponse.java
├── entity/                         # JPA Database Entities
│   ├── FertilizerBatchEntity.java
│   ├── RoleEntity.java
│   └── UserEntity.java
├── enums/                          # System Enums
│   └── Role.java
├── exception/                      # Global Exception Handlers & Custom Exceptions
│   ├── AccountBannedException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidTokenException.java
├── repository/                     # Spring Data JPA Repositories
│   ├── FertilizerBatchRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
├── security/                       # Spring Security & JWT components
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
└── service/                        # Business Logic Layer
    ├── AuthService.java
    ├── FertilizerBatchService.java
    └── impl/
        └── AuthServiceImpl.java
```

---

## 3. Database Schema & Domain Models

### `roles` Table
Mapped by `RoleEntity.java`.
- `role_id` (BIGINT, Primary Key, Identity)
- `role_name` (VARCHAR, Enum string representation, Unique, Nullable = false)
- `description` (VARCHAR, Nullable = false)

#### System Roles (`com.bandits.bhumisaara.enums.Role`):
1. `SYSTEM_ADMIN`
2. `GOVERNMENT_ADMIN`
3. `AGRARIAN_SERVICE_OFFICER`
4. `FARMER`
5. `PRIVATE_AGRO_DEALER`
6. `ORGANIC_FERTILIZER_PRODUCER`

### `users` Table
Mapped by `UserEntity.java`. Implements Spring Security `UserDetails`.
- `user_id` (BIGINT, Primary Key, Identity)
- `username` (VARCHAR, Unique, Nullable = false)
- `email` (VARCHAR, Unique, Nullable = false)
- `password` (VARCHAR, Encrypted BCrypt, Nullable = false)
- `role_id` (BIGINT, Foreign Key referencing `roles.role_id`)
- `is_banned` (BOOLEAN, Default: false)
- `is_assigned` (BOOLEAN, Default: false)
- `created_at` (TIMESTAMP, Updatable = false)

### `fertilizer_batches` Table
Mapped by `FertilizerBatchEntity.java`. Represents supply chain fertilizer batch records synced after blockchain transaction minting.
- `batch_id` (BIGINT, Primary Key, Identity)
- `token_id` (VARCHAR, Unique, Nullable = false) — *Note: Handled as String to support token IDs like "TK-0", "TK-1", or large blockchain string hashes.*
- `transaction_hash` (VARCHAR, Unique, Nullable = false) — Cryptographic transaction proof on blockchain.
- `importer_name` (VARCHAR, Nullable = false)
- `fertilizer_type` (VARCHAR, Nullable = false)
- `volume_kg` (INTEGER, Nullable = false)
- `minted_by_user_id` (BIGINT, Nullable = false) — References Government Admin user ID.
- `created_at` (TIMESTAMP, Updatable = false, set automatically via `@PrePersist`).

---

## 4. REST API Documentation

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | `RegisterRequest` | `AuthResponse` | Yes |
| `POST` | `/api/auth/login` | Authenticate user & issue JWT | `LoginRequest` | `AuthResponse` | Yes |
| `POST` | `/api/auth/refresh` | Refresh expired access token | `RefreshTokenRequest` | `AuthResponse` | Yes |
| `GET` | `/api/auth/me` | Fetch authenticated user profile | None | `AuthResponse` | No (JWT Required) |
| `GET` | `/api/auth/validate` | Validate JWT token string | Header or Query Param | `TokenValidationResponse` | Yes |

### Fertilizer Batches (`/api/batches`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/batches` | Record new minted fertilizer batch | `BatchRequestDTO` | `BatchResponseDTO` | Yes (Configurable in SecurityConfig) |
| `GET` | `/api/batches` | List all recorded batches | None | `List<BatchResponseDTO>` | Yes |
| `GET` | `/api/batches/{batchId}` | Get batch details by database ID | None | `BatchResponseDTO` | Yes |
| `GET` | `/api/batches/token/{tokenId}` | Get batch details by blockchain token ID | None | `BatchResponseDTO` | Yes |

---

## 5. Security & Authentication Architecture

1. **Stateless JWT Security**:
   - `SecurityConfig.java` configures `SessionCreationPolicy.STATELESS`.
   - Cors policy permits cross-origin requests from Next.js frontend (`app.cors.allowed-origins`).
   - `JwtAuthFilter` extracts Bearer tokens from incoming HTTP `Authorization` headers.

2. **Error Responses in Security Filter**:
   - Unauthenticated requests trigger `401 Unauthorized` with formatted JSON `ErrorResponse`.
   - Access denied actions trigger `403 Forbidden` with formatted JSON `ErrorResponse`.

3. **Global Exception Handling**:
   - `GlobalExceptionHandler.java` catches validation errors (`MethodArgumentNotValidException`, `ConstraintViolationException`), duplicate resource errors (`IllegalStateException`), and general errors, returning uniform `ErrorResponse` objects.

---

## 6. Commands & How to Run

### Granting Execution Permission to Maven Wrapper
```bash
chmod +x ./mvnw
```

### Compiling the Application
```bash
./mvnw compile
```

### Running Local Development Server
```bash
./mvnw spring-boot:run
```

### Environment Configuration
The application uses `me.paulschwarz:spring-dotenv`. Environment variables are loaded automatically from:
- `.env.development`
- `.env.staging`
- `.env.production`

Ensure PostgreSQL database credentials (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) and JWT secret keys (`APP_JWT_SECRET`) are properly configured in `.env.development`.

---

## 7. Guidelines for AI Agents

When implementing new features or modifying existing code in this repository, strictly adhere to the following rules:

1. **Naming & Package Conventions**:
   - Entities must be placed in `com.bandits.bhumisaara.entity` and suffixed with `Entity` (e.g., `UserEntity`).
   - Request DTOs must be placed in `com.bandits.bhumisaara.dto.request` and suffixed with `DTO` or `Request`.
   - Response DTOs must be placed in `com.bandits.bhumisaara.dto.response` and suffixed with `DTO` or `Response`.

2. **Data Types**:
   - Blockchain token IDs (`tokenId`) must always be treated as `String` to avoid numeric parsing errors on formatted string tokens (e.g., `"TK-0"`).

3. **Validation & Annotations**:
   - Always validate incoming controller request DTOs using `@Valid @RequestBody`.
   - Use Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Positive`) on request DTO fields.

4. **Service Layer**:
   - Service classes should be annotated with `@Service` and use `@RequiredArgsConstructor` for constructor injection of dependencies.
   - Use `@Transactional` for write methods and `@Transactional(readOnly = true)` for read operations.

5. **Security Configuration**:
   - When adding new REST endpoints, verify if they require authentication or public access, and update `SecurityConfig.java` accordingly.
