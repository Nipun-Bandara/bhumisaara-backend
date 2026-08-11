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
│   ├── AreaController.java         # Area lookup endpoints
│   ├── AuthController.java         # Authentication & Token validation endpoints
│   ├── FertilizerBatchController.java # Fertilizer batch minting & querying endpoints
│   ├── FertilizerRequestController.java # Farmer subsidy requests & officer review
│   ├── OfficerController.java      # Officer listing & area-assignment endpoints
│   └── DistributionController.java # Farmer handover / token-burn distribution endpoints
├── dto/                            # Data Transfer Objects
│   ├── request/                    # Incoming payload DTOs with validation
│   │   ├── AssignOfficerRequestDTO.java
│   │   ├── BatchRequestDTO.java
│   │   ├── FertilizerRequestCreateDTO.java
│   │   ├── FertilizerRequestReviewDTO.java
│   │   ├── HandoverRequestDTO.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── RegisterRequest.java
│   └── response/                   # Outgoing API response DTOs
│       ├── AreaResponseDTO.java
│       ├── AuthResponse.java
│       ├── BatchResponseDTO.java
│       ├── DistributionResponseDTO.java
│       ├── ErrorResponse.java
│       ├── FertilizerRequestResponseDTO.java
│       ├── OfficerResponseDTO.java
│       └── TokenValidationResponse.java
├── entity/                         # JPA Database Entities
│   ├── AreaEntity.java
│   ├── DistributionLogEntity.java
│   ├── FertilizerBatchEntity.java
│   ├── FertilizerRequestEntity.java
│   ├── RoleEntity.java
│   ├── UserEntity.java
│   └── UserQuotaEntity.java
├── enums/                          # System Enums
│   ├── RequestStatus.java
│   └── Role.java
├── exception/                      # Global Exception Handlers & Custom Exceptions
│   ├── AccountBannedException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidTokenException.java
├── repository/                     # Spring Data JPA Repositories
│   ├── AreaRepository.java
│   ├── DistributionLogRepository.java
│   ├── FertilizerBatchRepository.java
│   ├── FertilizerRequestRepository.java
│   ├── RoleRepository.java
│   ├── UserQuotaRepository.java
│   └── UserRepository.java
├── security/                       # Spring Security & JWT components
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
└── service/                        # Business Logic Layer
    ├── AreaService.java
    ├── AuthService.java
    ├── DistributionService.java
    ├── FertilizerBatchService.java
    ├── FertilizerRequestService.java
    ├── OfficerService.java
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
- `wallet_address` (VARCHAR(42), Unique, Nullable) — EVM address, normalised to lowercase on persist/update; blank input collapses to `NULL` so the unique index isn't tripped by repeated `''`.
- `role_id` (BIGINT, Foreign Key referencing `roles.role_id`)
- `area_id` (BIGINT, Foreign Key referencing `areas.area_id`, Nullable) — set by `POST /api/officers/assign`.
- `is_banned` (BOOLEAN, Default: false)
- `is_assigned` (BOOLEAN, Default: false) — set true when an officer is assigned to an area.
- `created_at` (TIMESTAMP, Updatable = false)

### `areas` Table
Mapped by `AreaEntity.java`. Geographic areas an agrarian service officer can be assigned to.
- `area_id` (BIGINT, Primary Key, Identity)
- `area_name` (VARCHAR(100), Nullable = false)
- `district` (VARCHAR(100), Nullable = false)
- Unique constraint `uq_area_name_district` on (`area_name`, `district`).
- *Note: no create/update endpoint exists yet — rows must currently be seeded manually. `GET /api/areas` returns an empty list until then, and the Assign Officers screen shows "No areas exist yet".*

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

### `distribution_logs` Table
Mapped by `DistributionLogEntity.java`. Records physical handover of fertilizer to a farmer and the corresponding blockchain token burn.
- `distribution_id` (BIGINT, Primary Key, Identity)
- `token_id` (VARCHAR, Nullable = false) — ERC-1155 token ID; stored as `String` per the `tokenId` convention in §7 (avoids `uint256` overflow on `Integer`/`Long`).
- `batch_id` (BIGINT, Nullable = false) — References `fertilizer_batches.batch_id`.
- `farmer_id` (BIGINT, Nullable = false) — References `users.user_id`.
- `officer_id` (BIGINT, Nullable = false) — References `users.user_id` (Agrarian Extension Officer who performed the handover).
- `amount_dispensed_kg` (INTEGER, Nullable = false)
- `burn_transaction_hash` (VARCHAR, Unique, Nullable = false) — Cryptographic proof of the Polygon token burn; also used to reject duplicate/replayed handover calls.
- `created_at` (TIMESTAMP, Updatable = false, set automatically via `@PrePersist`).

### `fertilizer_requests` Table
Mapped by `FertilizerRequestEntity.java`. A farmer's subsidy request and its review by the agrarian service officer of that farmer's area.
- `request_id` (BIGINT, Primary Key, Identity)
- `farmer_id` (BIGINT, Foreign Key referencing `users.user_id`, Nullable = false) — a real `@ManyToOne`, because the officer queue is scoped by `farmer -> area`.
- `season` (VARCHAR(50), Nullable = false) — e.g. `"Maha 2025/2026"`, `"Yala 2026"`.
- `fertilizer_type` (VARCHAR, Nullable = false)
- `requested_kg` (INTEGER, Nullable = false)
- `approved_kg` (INTEGER, Nullable) — null until reviewed; may be below `requested_kg` on a partial approval.
- `status` (VARCHAR(20), Nullable = false, `@Enumerated(EnumType.STRING)`) — `RequestStatus`: `PENDING`, `APPROVED`, `REJECTED`, `COLLECTED`. Defaults to `PENDING`.
- `reviewed_by_officer_id` (BIGINT, Foreign Key referencing `users.user_id`, Nullable)
- `reviewed_at` (TIMESTAMP, Nullable)
- `batch_id` (BIGINT, Nullable), `sack_serial` (VARCHAR, Nullable), `burn_tx_hash` (VARCHAR, Unique, Nullable), `collected_at` (TIMESTAMP, Nullable) — collection fields. **No endpoint writes these yet**; collection still runs through `POST /api/v1/distributions`, and `COLLECTED` is therefore currently unreachable.
- `created_at` (TIMESTAMP, Updatable = false, `@PrePersist`)
- *Note: a farmer may hold only one `PENDING` request per (season, fertilizer_type); a rejected one can be re-filed.*

### `user_quotas` Table
Mapped by `UserQuotaEntity.java`. Tracks each farmer's remaining fertilizer allowance per fertilizer type.
- `quota_id` (BIGINT, Primary Key, Identity)
- `farmer_id` (BIGINT, Nullable = false) — References `users.user_id`.
- `fertilizer_type` (VARCHAR, Nullable = false)
- `remaining_kg` (INTEGER, Nullable = false)
- `updated_at` (TIMESTAMP, Nullable = false)
- Unique constraint on (`farmer_id`, `fertilizer_type`) — one quota row per farmer per fertilizer type.
- *Note: no allocation/replenishment endpoint exists yet — rows must currently be seeded manually. A farmer with no row for a given fertilizer type is rejected by `POST /api/v1/distributions` (400, "No quota allocated").*

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

### Distributions (`/api/v1/distributions`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/v1/distributions` | Record a farmer handover (deducts batch volume + farmer quota, logs the token burn) | `HandoverRequestDTO` | `DistributionResponseDTO` | No (JWT Required) |

### Users (`/api/users`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/users/me/wallet` | The authenticated user's linked wallet address (null if never connected) | None | `WalletAddressResponseDTO` | No (JWT, any role) |
| `PATCH` | `/api/users/me/wallet` | Link the connected wallet to the authenticated user. Lowercased before the uniqueness check; 409 if another account already holds it | `UpdateWalletAddressRequestDTO` | `WalletAddressResponseDTO` | No (JWT, any role) |

> Called automatically by the frontend's `useWalletAddressSync` hook (mounted in
> `app/(ui)/layout.tsx`) the first time thirdweb reports a connected address, so
> `users.wallet_address` is populated without a manual step. Re-sending the same
> address is a no-op; connecting a different wallet replaces the stored one.

### Areas (`/api/areas`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/areas` | List all areas, ordered by district then area name | None | `List<AreaResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |

### Officers (`/api/officers`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/officers` | List users holding `AGRARIAN_SERVICE_OFFICER`, with their current area. Optional `?assigned=true\|false` filter; omitted returns all | None | `List<OfficerResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `POST` | `/api/officers/assign` | Assign an officer to an area (sets `users.area_id` + `users.is_assigned`). Re-assigning moves the officer rather than failing | `AssignOfficerRequestDTO` | `OfficerResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |

### Fertilizer Requests (`/api/fertilizer-requests`)

The farmer and officer identities come from the JWT principal, never the payload — neither `farmer_id` nor `reviewed_by_officer_id` is client-supplied.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/fertilizer-requests` | Raise a request for the authenticated farmer | `FertilizerRequestCreateDTO` | `FertilizerRequestResponseDTO` (201) | No (JWT + `FARMER`) |
| `GET` | `/api/fertilizer-requests/me` | The authenticated farmer's own requests, newest first | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `FARMER`) |
| `GET` | `/api/fertilizer-requests/pending` | Review queue: pending requests from farmers in the authenticated officer's area, **oldest first** (FIFO) | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/fertilizer-requests/area` | Area history: every request from farmers in the officer's area, **newest first**, whoever reviewed it. Optional `?status=PENDING\|APPROVED\|REJECTED\|COLLECTED`; omitted returns all | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `PATCH` | `/api/fertilizer-requests/{requestId}/review` | Approve or reject a pending request | `FertilizerRequestReviewDTO` | `FertilizerRequestResponseDTO` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |

Review rules enforced in `FertilizerRequestService`:
- The request must still be `PENDING` (409 otherwise).
- The officer's `area_id` must equal the farmer's `area_id` (403 otherwise) — this is what "approved by the officer in that area" means in code.
- `status` must be `APPROVED` or `REJECTED`; `approved_kg` is required on approve, must be ≤ `requested_kg`, and is cleared on reject.
- A farmer with no `area_id` cannot raise a request at all (400) — nobody would be able to review it.

> These are the first endpoints in the codebase to enforce a role. They rely on
> `@PreAuthorize` + the `@EnableMethodSecurity` already set on `SecurityConfig`;
> `JwtAuthFilter` supplies the `ROLE_<name>` authority from the token's `role`
> claim. `SecurityConfig.authorizeHttpRequests` needs no change — they are
> covered by the existing `anyRequest().authenticated()`.

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
