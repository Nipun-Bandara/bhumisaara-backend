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
│   ├── BatchTransferController.java # Admin → officer area demand, transfers & history
│   ├── SackController.java         # Sack listing (label sheet) & backfill
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
│   │   ├── RegisterRequest.java
│   │   ├── SackValidationRequestDTO.java
│   │   └── TransferRequestDTO.java
│   └── response/                   # Outgoing API response DTOs
│       ├── AreaDemandResponseDTO.java
│       ├── AreaResponseDTO.java
│       ├── AuthResponse.java
│       ├── PendingCollectionResponseDTO.java
│       ├── SackResponseDTO.java
│       ├── SackValidationResponseDTO.java
│       ├── TransferResponseDTO.java
│       ├── BatchResponseDTO.java
│       ├── DistributionResponseDTO.java
│       ├── ErrorResponse.java
│       ├── FertilizerRequestResponseDTO.java
│       ├── OfficerResponseDTO.java
│       └── TokenValidationResponse.java
├── entity/                         # JPA Database Entities
│   ├── AreaEntity.java
│   ├── BatchTransferEntity.java
│   ├── HandoverSackEntity.java
│   ├── SackEntity.java
│   ├── DistributionLogEntity.java
│   ├── FertilizerBatchEntity.java
│   ├── FertilizerRequestEntity.java
│   ├── RoleEntity.java
│   ├── UserEntity.java
│   └── UserQuotaEntity.java
├── enums/                          # System Enums
│   ├── RequestStatus.java
│   ├── Role.java
│   └── SackStatus.java
├── exception/                      # Global Exception Handlers & Custom Exceptions
│   ├── AccountBannedException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidTokenException.java
├── repository/                     # Spring Data JPA Repositories
│   ├── AreaRepository.java
│   ├── BatchTransferRepository.java
│   ├── HandoverSackRepository.java
│   ├── SackRepository.java
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
    ├── BatchTransferService.java
    ├── SackService.java
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

### `sacks` Table
Mapped by `SackEntity.java`. The physical, individually labelled sacks a batch is broken into. Created automatically inside the `POST /api/batches` transaction — a batch never exists without its sacks.
- `sack_id` (BIGINT, Primary Key, Identity)
- `batch_id` (BIGINT, Nullable = false) — References `fertilizer_batches.batch_id`.
- `serial` (VARCHAR(32), Unique, Nullable = false) — printed on the sack and scanned in the field, formatted `XXXX-XXXX-XXXX`. **Never sequential**: 12 random uppercase alphanumerics from `SecureRandom`, re-rolled if the candidate is already taken (`SackService.generateUniqueSerial`). A guessable serial would let anyone print a label that scans as genuine.
- `weight_kg` (INTEGER, Nullable = false) — 50kg per sack; the final sack of a batch carries the remainder when `volume_kg` isn't divisible by 50.
- `status` (VARCHAR(20), Nullable = false, `@Enumerated(EnumType.STRING)`) — `SackStatus`: `AT_CENTRAL`, `WITH_OFFICER`, `DELIVERED`. Starts `AT_CENTRAL`.
- `held_by_user_id` (BIGINT, Nullable) — current custodian; set to the minting admin on creation, reassigned to the officer on transfer.
- `created_at` (TIMESTAMP, Updatable = false, `@PrePersist`)
- *Note: a single mint may not produce more than 5 000 sacks (`SackService.MAX_SACKS_PER_BATCH`, ≈250 tonnes).*

### `batch_transfers` Table
Mapped by `BatchTransferEntity.java`. A government admin → agrarian service officer stock movement, proven by an ERC-1155 `safeTransferFrom` on Polygon.
- `transfer_id` (BIGINT, Primary Key, Identity)
- `batch_id` (BIGINT, Nullable = false) — References `fertilizer_batches.batch_id`.
- `token_id` (VARCHAR, Nullable = false) — stored as `String` per the `tokenId` convention in §7.
- `from_user_id` (BIGINT, Nullable = false) — the sending government admin, taken from the JWT and never from the payload.
- `to_officer_id` (BIGINT, Nullable = false) — References `users.user_id`.
- `amount_kg` (INTEGER, Nullable = false)
- `transaction_hash` (VARCHAR, Unique, Nullable = false) — also the replay guard against a double-submitted transfer.
- `created_at` (TIMESTAMP, Updatable = false, `@PrePersist`)
- *Note: a transfer moves custody, it does not consume stock, so it deliberately never touches `fertilizer_batches.volume_kg` — only the sacks change hands. Volume is deducted when a farmer collects (`POST /api/v1/distributions`).*

### `distribution_logs` Table
Mapped by `DistributionLogEntity.java`. Records physical handover of fertilizer to a farmer and the corresponding blockchain token burn.
- `distribution_id` (BIGINT, Primary Key, Identity)
- `token_id` (VARCHAR, Nullable = false) — ERC-1155 token ID; stored as `String` per the `tokenId` convention in §7 (avoids `uint256` overflow on `Integer`/`Long`).
- `batch_id` (BIGINT, Nullable = false) — References `fertilizer_batches.batch_id`.
- `farmer_id` (BIGINT, Nullable = false) — References `users.user_id`.
- `officer_id` (BIGINT, Nullable = false) — References `users.user_id` (Agrarian Extension Officer who performed the handover).
- `request_id` (BIGINT, Nullable) — the approved `fertilizer_requests` row this handover fulfilled. Nullable only because rows written before requests were linked have nothing to point at; every new row sets it.
- `amount_dispensed_kg` (INTEGER, Nullable = false)
- `burn_transaction_hash` (VARCHAR, Unique, Nullable = false) — Cryptographic proof of the Polygon token burn; also used to reject duplicate/replayed handover calls.
- `disputed` (BOOLEAN, Nullable = false, `@ColumnDefault("false")`), `disputed_at` (TIMESTAMP, Nullable) — the farmer's "I did not receive this" flag. Raising it reverses nothing; the tokens are burned and the sacks consumed, so it marks the record for a human.
- `created_at` (TIMESTAMP, Updatable = false, set automatically via `@PrePersist`).

> **Why `@ColumnDefault` matters here:** under `ddl-auto: update` Hibernate adds a
> column with a bare `ALTER TABLE`, which fails on a NOT NULL column if the table
> already holds rows. `disputed` and `fertilizer_requests.collected_kg` therefore
> carry an explicit default so the migration succeeds against a populated dev DB.

### `handover_sacks` Table
Mapped by `HandoverSackEntity.java`. The sacks that backed one handover to a farmer.
- `id` (BIGINT, Primary Key, Identity)
- `distribution_id` (BIGINT, Nullable = false) — References `distribution_logs.distribution_id`.
- `sack_serial` (VARCHAR(32), **Unique**, Nullable = false) — the consume-once guarantee: a physical sack can back exactly one handover, ever. Enforced by the database, not just the service, so a replayed or concurrent request cannot spend the same sack twice.
- `weight_kg` (INTEGER, Nullable = false)

### `fertilizer_requests` Table
Mapped by `FertilizerRequestEntity.java`. A farmer's subsidy request and its review by the agrarian service officer of that farmer's area.
- `request_id` (BIGINT, Primary Key, Identity)
- `farmer_id` (BIGINT, Foreign Key referencing `users.user_id`, Nullable = false) — a real `@ManyToOne`, because the officer queue is scoped by `farmer -> area`.
- `season` (VARCHAR(50), Nullable = false) — e.g. `"Maha 2025/2026"`, `"Yala 2026"`.
- `fertilizer_type` (VARCHAR, Nullable = false)
- `requested_kg` (INTEGER, Nullable = false)
- `approved_kg` (INTEGER, Nullable) — null until reviewed; may be below `requested_kg` on a partial approval.
- `collected_kg` (INTEGER, Nullable = false, `@ColumnDefault("0")`) — how much of `approved_kg` the farmer has physically collected. The ceiling every handover is checked against: a farmer approved for 50kg cannot walk away with two 50kg sacks across two visits.
- `status` (VARCHAR(20), Nullable = false, `@Enumerated(EnumType.STRING)`) — `RequestStatus`: `PENDING`, `APPROVED`, `REJECTED`, `PARTIALLY_COLLECTED`, `COLLECTED`. Defaults to `PENDING`. The handover flow sets `PARTIALLY_COLLECTED` while `collected_kg < approved_kg` and `COLLECTED` once it reaches it.
- `reviewed_by_officer_id` (BIGINT, Foreign Key referencing `users.user_id`, Nullable)
- `reviewed_at` (TIMESTAMP, Nullable)
- `batch_id` (BIGINT, Nullable), `sack_serial` (VARCHAR, Nullable), `burn_tx_hash` (VARCHAR, Unique, Nullable) — legacy single-sack collection fields, superseded by `distribution_logs` + `handover_sacks`, which handle several sacks and several visits per request. Nothing writes them; don't add new readers.
- `collected_at` (TIMESTAMP, Nullable) — set on the first handover against the request.
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
| `POST` | `/api/batches` | Record new minted fertilizer batch **and generate its 50kg sacks** | `BatchRequestDTO` | `BatchResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`) |
| `GET` | `/api/batches` | List all recorded batches | None | `List<BatchResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`/`AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/batches/{batchId}` | Get batch details by database ID | None | `BatchResponseDTO` | No (same three roles) |
| `GET` | `/api/batches/token/{tokenId}` | Get batch details by blockchain token ID | None | `BatchResponseDTO` | No (same three roles) |

> Reads stay open to `AGRARIAN_SERVICE_OFFICER` because the officer handover
> screen picks the batch it dispenses from out of `GET /api/batches`; locking
> reads to the admin would break that flow.

### Sacks (`/api/v1/sacks`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/sacks/batch/{batchId}` | Every sack of one batch, in label order (drives the printable label sheet) | None | `List<SackResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`) |
| `POST` | `/api/v1/sacks/backfill/{batchId}` | Generate sacks for a batch minted before sacks existed. Idempotent — a batch that already has sacks is returned untouched | None | `List<SackResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`) |

### Batch Transfers (`/api/v1/transfers`)

The sending admin comes from the JWT principal, never the payload.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/transfers/demand` | Distribution queue: one row per (area, fertilizer type) with approved demand, largest shortfall first | None | `List<AreaDemandResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`) |
| `POST` | `/api/v1/transfers` | Record a confirmed on-chain transfer and move the scanned sacks into the officer's custody | `TransferRequestDTO` | `TransferResponseDTO` (201) | No (JWT + `GOVERNMENT_ADMIN`) |
| `GET` | `/api/v1/transfers` | Admin transfer history, newest first | None | `List<TransferResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`) |

Demand maths in `BatchTransferService.getAreaDemand()`:
- `approvedKg` sums `fertilizer_requests.approved_kg` where `status IN ('APPROVED','COLLECTED')`, grouped by the **farmer's** area and fertilizer type. `COLLECTED` still counts — it was fulfilled from stock the officer was sent, and dropping it would make a settled area look under-supplied and invite a second delivery.
- `transferredKg` sums `batch_transfers.amount_kg` already sent to that area's officer for that fertilizer type (the type lives on the batch, so it's an ad-hoc join).
- `outstandingKg` is `max(0, approved − transferred)`.
- The officer is **resolved from the area**, never chosen by the admin — one serving officer per area, lowest `user_id` wins if several share one. An area with demand but no officer still appears, with `officerId`/`officerWallet` null so the UI can disable the row.

Validations enforced in `recordTransfer` (one `@Transactional` method):
- duplicate `transaction_hash` → 409 (replay guard);
- every serial must exist, belong to `batchId`, and still be `AT_CENTRAL` (duplicates in the list are collapsed);
- the sack weights must sum **exactly** to `amountKg`;
- `toOfficerId` must hold `AGRARIAN_SERVICE_OFFICER` **and** have a `wallet_address`.

### Distributions (`/api/v1/distributions`)

The officer → farmer handover. **The tokens are burned straight from the officer's
own wallet — they are never transferred to the farmer first.** The farmer is the
recipient of record in Postgres and the burn is the on-chain proof the stock left
circulation. One transaction, no transfer step. Do not add one.

The acting officer always comes from the JWT, and the farmer from the request
being fulfilled — neither is client-supplied.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/distributions/pending` | The officer's collection queue: approved requests from farmers in **their own area** that still have stock owed | None | `List<PendingCollectionResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `POST` | `/api/v1/distributions/validate-sack` | Pre-flight check on a scanned sack, with the exact rejection reason | `SackValidationRequestDTO` | `SackValidationResponseDTO` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `POST` | `/api/v1/distributions` | Record a handover whose burn already confirmed: consumes the sacks, advances the request, deducts batch volume | `HandoverRequestDTO` | `DistributionResponseDTO` (201) | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/v1/distributions/officer` | Handovers the calling officer performed, newest first | None | `List<DistributionResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/v1/distributions/farmer` | Handovers the calling farmer received, newest first | None | `List<DistributionResponseDTO>` | No (JWT + `FARMER`) |
| `POST` | `/api/v1/distributions/{id}/dispute` | "I did not receive this" — flags the record, reverses nothing. Only the farmer named on it may raise it | None | `DistributionResponseDTO` | No (JWT + `FARMER`) |

Sack rules, applied in `validateSack` **and re-run inside `recordHandover`** — a
client that skipped validation (or lied about `alreadyScannedKg`) must not get
further. Each has its own message:
- the serial must exist;
- its status must be `WITH_OFFICER` (a `DELIVERED` sack says so; an `AT_CENTRAL` one says it was never transferred to you);
- `held_by_user_id` must be the calling officer;
- it must not already appear in `handover_sacks` (consume-once);
- `alreadyScannedKg + weight` must not exceed `approved_kg - collected_kg` — the quota ceiling, named in the message.

`recordHandover` additionally, in one `@Transactional`:
- rejects a duplicate `burn_transaction_hash` (replay guard);
- verifies the request's farmer is in the officer's area;
- verifies sack weights sum **exactly** to `amount_dispensed_kg`, and that every sack belongs to the burned batch;
- verifies `farmerWallet` matches the farmer's stored `wallet_address` (case-insensitive);
- writes `distribution_logs` + `handover_sacks`, sets those sacks to `DELIVERED` held by the farmer;
- increments `collected_kg` and moves the request to `PARTIALLY_COLLECTED` / `COLLECTED`;
- **deducts `fertilizer_batches.volume_kg`** — the burn destroyed the stock, so unlike the admin → officer transfer this deduction is correct here.

> The pre-handover quota check against `user_quotas` was removed: `approved_kg`
> on the request is now the ceiling, and it is enforced per-visit through
> `collected_kg`. `user_quotas` is unseeded and was rejecting every handover.

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
