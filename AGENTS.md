# Agent Guidelines & Repository Documentation (AGENTS.md)

Welcome to the **Bhumisaara Backend** repository. This document serves as the primary technical specification, architecture guide, and operational manual for AI agents and developers working on this Spring Boot application.

---

## 1. Project Overview

**Bhumisaara Backend** is a Java Spring Boot Web2.5 supply chain application designed for agricultural and fertilizer tracking (including blockchain tokenization proof). It integrates standard RESTful APIs with PostgreSQL database persistence and JWT-based authentication.

### Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.4.x / 4.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`)
- **Database**: PostgreSQL (the unused MySQL runtime driver was removed)
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
│   ├── DistributionController.java # Farmer handover / token-burn distribution endpoints
│   ├── SubsidyCreditController.java # Credit issuance, farmer balance, reconciliation
│   ├── ProductListingController.java # Seller listings + the farmer-facing marketplace
│   ├── MarketOrderController.java  # Orders & the mandatory farmer confirmation
│   └── RedemptionClaimController.java # Seller claims & government review
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
│   │   ├── UpdateProfileRequestDTO.java
│   │   └── TransferRequestDTO.java
│   └── response/                   # Outgoing API response DTOs
│       ├── AreaDemandResponseDTO.java
│       ├── AreaResponseDTO.java
│       ├── AuthResponse.java
│       ├── PendingCollectionResponseDTO.java
│       ├── ProfileResponseDTO.java
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
│   ├── SackStatus.java
│   ├── TokenType.java              # STOCK vs SUBSIDY_CREDIT — see §2.1
│   ├── ListingStatus.java
│   ├── OrderStatus.java
│   └── ClaimStatus.java
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
    ├── CreditMath.java             # THE organic 1.5x conversion. One copy, integer maths
    ├── SubsidyCreditService.java   # Credit issuance & farmer balances
    ├── ProductListingService.java  # Seller listings; isOrganic derived from role
    ├── MarketOrderService.java     # Orders; only the farmer can complete one
    ├── RedemptionClaimService.java # Claims; the seller signs the settling burn
    ├── CreditOversightService.java # Reconciliation & seller anomaly flags
    └── impl/
        └── AuthServiceImpl.java
```

---

## 2.1 Two Token Types — read this before touching any token code

The ERC-1155 contract now carries **two kinds of token**, and they must never be
summed, swapped or confused. `com.bandits.bhumisaara.enums.TokenType` names them,
and every table that records a mint carries a `token_type` column.

| | `STOCK` | `SUBSIDY_CREDIT` |
|---|---|---|
| Backed by | fertilizer in a warehouse | the treasury |
| Created by | `POST /api/v1/batches` (a real import) | `POST /api/v1/credits/issue` (a funded season) |
| 1 token means | 1 kg of physical stock | an entitlement to 1 kg chemical **or 1.5 kg organic** |
| Burned when | a farmer physically collects | a seller redeems the credit for cash |
| Tracked in | `fertilizer_batches`, `sacks`, `batch_transfers`, `distribution_logs` | `subsidy_credit_issuances`, `market_orders`, `redemption_claims` |

`batch_transfers` and `distribution_logs` carry no `token_type` column: they are
reachable only from a `fertilizer_batches` row, so they are STOCK by
construction. Adding one would have meant editing the government → officer →
farmer flow, which is explicitly out of bounds.

**The organic 1.5× conversion lives in exactly one place:**
`com.bandits.bhumisaara.service.CreditMath`. It is integer arithmetic
(`× 2 / 3` up, `× 3 / 2` down) so a browser's floating point can never disagree
with the server in the farmer's favour. The frontend mirrors it in
`lib/marketplace.ts` for the live preview, but **every order re-derives the cost
server-side** — no client-computed total is ever stored.

### Credit token ids are per-season
Every farmer issued credits for a given season shares **one** ERC-1155 token id.
That is what makes a season's credits fungible between farmers, and what
`balanceOf(wallet, tokenId)` needs to read a balance at all. The season's first
issuance creates the token (`mintTo`); every later one adds supply to it
(`mintAdditionalSupplyTo`). `SubsidyCreditService.requireSeasonTokenIdMatches`
rejects an issuance whose token id disagrees with the season's.

### The backend has no web3 client
It never did, and this feature did not add one. Every chain interaction is
signed and read **in the browser**; the backend records confirmed hashes. So
`GET /api/v1/credits/balance` returns the Postgres *ledger* position plus the
season token ids, and the client reads the authoritative on-chain balance
itself. A gap between the two is real information — it means credits moved
off-platform — and both figures are shown rather than reconciled away.

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
- `full_name` (VARCHAR(120), Nullable), `address` (VARCHAR(255), Nullable), `contact_number` (VARCHAR(20), Nullable) — the editable profile, maintained by the user through `PUT /api/v1/users/me/profile`. All nullable: accounts exist before anyone fills a profile in. `username` stays the login identity; `full_name` is only a display name. `address` doubles as the officer's agrarian centre — same column, different label on screen.
- `role_id` (BIGINT, Foreign Key referencing `roles.role_id`)
- `area_id` (BIGINT, Foreign Key referencing `areas.area_id`, Nullable) — set by `POST /api/v1/officers/assign`.
- `is_banned` (BOOLEAN, Default: false)
- `is_assigned` (BOOLEAN, Default: false) — set true when an officer is assigned to an area.
- `created_at` (TIMESTAMP, Updatable = false)

### `areas` Table
Mapped by `AreaEntity.java`. Geographic areas an agrarian service officer can be assigned to.
- `area_id` (BIGINT, Primary Key, Identity)
- `area_name` (VARCHAR(100), Nullable = false)
- `district` (VARCHAR(100), Nullable = false)
- Unique constraint `uq_area_name_district` on (`area_name`, `district`).
- *Note: no create/update endpoint exists yet — rows must currently be seeded manually. `GET /api/v1/areas` returns an empty list until then, and the Assign Officers screen shows "No areas exist yet".*

### `fertilizer_batches` Table
Mapped by `FertilizerBatchEntity.java`. Represents supply chain fertilizer batch records synced after blockchain transaction minting.
- `batch_id` (BIGINT, Primary Key, Identity)
- `token_id` (VARCHAR, Unique, Nullable = false) — *Note: Handled as String to support token IDs like "TK-0", "TK-1", or large blockchain string hashes.*
- `transaction_hash` (VARCHAR, Unique, Nullable = false) — Cryptographic transaction proof on blockchain.
- `importer_name` (VARCHAR, Nullable = false)
- `fertilizer_type` (VARCHAR, Nullable = false)
- `volume_kg` (INTEGER, Nullable = false)
- `minted_by_user_id` (BIGINT, Nullable = false) — the minting Government Admin, **taken from the JWT**, never from the request body.
- `created_at` (TIMESTAMP, Updatable = false, set automatically via `@PrePersist`).

### `sacks` Table
Mapped by `SackEntity.java`. The physical, individually labelled sacks a batch is broken into. Created automatically inside the `POST /api/v1/batches` transaction — a batch never exists without its sacks.
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

### `subsidy_credit_issuances` Table
Mapped by `SubsidyCreditIssuanceEntity.java`. One season's subsidy credits, minted to one farmer's wallet. Backed by the treasury, not by goods — see §2.1.
- `issuance_id` (BIGINT, Primary Key, Identity)
- `farmer_id` (BIGINT, Nullable = false) — References `users.user_id`.
- `season` (VARCHAR(50), Nullable = false) — matches `fertilizer_requests.season`.
- `credits_kg` (INTEGER, Nullable = false)
- `token_id` (VARCHAR, Nullable = false) — the ERC-1155 id the season's credits live under, `String` per §7. **Not in the original spec but load-bearing**: without it no `balanceOf` read is possible, and every farmer in a season must share one id.
- `token_type` (VARCHAR(20), Nullable = false, `@ColumnDefault("'SUBSIDY_CREDIT'")`) — always `SUBSIDY_CREDIT`.
- `transaction_hash` (VARCHAR, **Unique**, Nullable = false) — the replay guard.
- `issued_by_user_id` (BIGINT, Nullable = false) — the issuing admin, **from the JWT**.
- `created_at` (TIMESTAMP, Updatable = false, `@PrePersist`)
- Unique constraint `uq_issuance_farmer_season` on (`farmer_id`, `season`) — **one issuance per farmer per season**, enforced by the database as well as the service so two admins clicking at once can't double-fund.

### `product_listings` Table
Mapped by `ProductListingEntity.java`. What a dealer or producer offers farmers.
- `listing_id` (BIGINT, Primary Key, Identity)
- `seller_id` (BIGINT, Nullable = false) — **always from the JWT**, never a payload id.
- `product_name` (VARCHAR(120)), `fertilizer_type` (VARCHAR(60)), `description` (VARCHAR(1000), Nullable)
- `is_organic` (BOOLEAN, Nullable = false) — **derived from the seller's role on every write**, never accepted from the client. It sets the credit conversion rate, so a dealer who could flag a listing organic would be charging 1 credit for 1.5kg.
- `price_lkr_per_kg` (INTEGER, Nullable = false), `available_kg` (INTEGER, Nullable = false)
- `is_subsidy_eligible` (BOOLEAN, Nullable = false, `@ColumnDefault("true")`)
- `status` (VARCHAR(20), Nullable = false, `@ColumnDefault("'ACTIVE'")`) — `ListingStatus`: `ACTIVE`, `PAUSED`, `SOLD_OUT`. **`SOLD_OUT` is derived from `available_kg`**, never settable directly (409 if sent).
- `created_at`, `updated_at` (TIMESTAMP, `@PrePersist`/`@PreUpdate`)

### `market_orders` Table
Mapped by `MarketOrderEntity.java`. **The anti-fraud mechanism — see the flow rules in §4.**
- `order_id` (BIGINT, Primary Key, Identity)
- `listing_id` (BIGINT, Nullable = false), `farmer_id` (BIGINT, Nullable = false)
- `seller_id` (BIGINT, Nullable = false) — denormalised from the listing so a re-assigned listing can't move an order.
- `quantity_kg` (INTEGER, Nullable = false)
- `credits_used` (INTEGER, Nullable = false), `cash_amount_lkr` (INTEGER, Nullable = false) — **both server-derived through `CreditMath`**; a client total is never stored. Cash is *recorded, never processed* — no payment rail exists.
- `status` (VARCHAR(25), Nullable = false, `@ColumnDefault("'PENDING_CONFIRMATION'")`) — `OrderStatus`.
- `token_type` (VARCHAR(20), Nullable = false, `@ColumnDefault("'SUBSIDY_CREDIT'")`)
- `credit_transfer_hash` (VARCHAR, **Unique**, Nullable) — null until the farmer confirms, and permanently null on a cash-only order.
- `created_at`, `confirmed_at`, `completed_at`, `disputed_at` (TIMESTAMP)
- *Note: `confirmed_at` is when the **seller** marked goods ready, not the farmer's confirmation. The farmer's confirmation sets `completed_at` — it is the same action that moves the credits.*

### `redemption_claims` Table
Mapped by `RedemptionClaimEntity.java`. A seller settling credits with the treasury.
- `claim_id` (BIGINT, Primary Key, Identity)
- `seller_id` (BIGINT, Nullable = false) — **from the JWT**.
- `credits_claimed` (INTEGER, Nullable = false)
- `status` (VARCHAR(20), Nullable = false, `@ColumnDefault("'SUBMITTED'")`) — `ClaimStatus`: `SUBMITTED`, `APPROVED`, `PAID`, `REJECTED`.
- `token_type` (VARCHAR(20), Nullable = false, `@ColumnDefault("'SUBSIDY_CREDIT'")`)
- `burn_transaction_hash` (VARCHAR, **Unique**, Nullable) — null until the seller burns.
- `submitted_at` (TIMESTAMP, Updatable = false), `processed_at` (TIMESTAMP, Nullable), `processed_by_user_id` (BIGINT, Nullable)

> **Why redemption is two steps.** The spec reads "on approval the credits are
> burned". They can't be — an ERC-1155 balance is destroyable only by its holder
> (or an approved operator), and the credits sit in the **seller's** wallet. A
> government admin has no way to burn them without the seller first granting
> `setApprovalForAll`, which would be a strictly worse security posture. So
> `APPROVED` is the admin authorising payment and `PAID` is the seller's burn
> settling it. Nothing counts as redeemed until that burn hash lands.

---

## 4. REST API Documentation

**The `/api/v1` prefix is a servlet context path, not something controllers
repeat.** `application.yml` sets `server.servlet.context-path: /api/v1`, so
`@RequestMapping` values are plain (`/batches`, `/auth`) and the tables below
show the full external URL a client calls.

Two things match the path **below** the context path and must never repeat it:
- `SecurityConfig.authorizeHttpRequests` — Spring Security matches the path within the application, so the permitAll list is `/auth/login`, not `/api/v1/auth/login`;
- `JwtAuthFilter.shouldNotFilter` — compares `request.getServletPath()`, which also excludes the context path. Keep it in step with the permitAll list.

The frontend mirrors this: `NEXT_PUBLIC_API_URL` points at
`http://localhost:8080/api/v1` and `utils/apiPaths.ts` entries are plain too.

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user | `RegisterRequest` | `AuthResponse` | Yes |
| `POST` | `/api/v1/auth/login` | Authenticate user & issue JWT | `LoginRequest` | `AuthResponse` | Yes |
| `POST` | `/api/v1/auth/refresh` | Refresh expired access token | `RefreshTokenRequest` | `AuthResponse` | Yes |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile | None | `AuthResponse` | No (JWT Required) |
| `GET` | `/api/v1/auth/validate` | Validate JWT token string | Header or Query Param | `TokenValidationResponse` | Yes |

### Fertilizer Batches (`/api/v1/batches`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/v1/batches` | Record new minted fertilizer batch **and generate its 50kg sacks** | `BatchRequestDTO` | `BatchResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`) |
| `GET` | `/api/v1/batches` | List all recorded batches | None | `List<BatchResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`/`AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/v1/batches/{batchId}` | Get batch details by database ID | None | `BatchResponseDTO` | No (same three roles) |
| `GET` | `/api/v1/batches/token/{tokenId}` | Get batch details by blockchain token ID | None | `BatchResponseDTO` | No (same three roles) |

> Reads stay open to `AGRARIAN_SERVICE_OFFICER` because the officer handover
> screen picks the batch it dispenses from out of `GET /api/v1/batches`; locking
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

### Subsidy Credits (`/api/v1/credits`)

The issuing admin and the balance-reading farmer both come from the JWT. See §2.1 for why the balance endpoint returns a ledger position rather than a chain read.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/credits/seasons` | Seasons an admin may issue against — filed requests plus already-funded seasons | None | `List<String>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `GET` | `/api/v1/credits/eligible-farmers?season=` | Every farmer, annotated with `alreadyIssued` / `canIssue`. Ineligible rows are **returned, not filtered** — the admin needs to see *why* a name is unselectable | None | `List<EligibleFarmerResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`) |
| `POST` | `/api/v1/credits/issue` | Record a credit mint that already confirmed on-chain | `IssueCreditsRequestDTO` | `CreditIssuanceResponseDTO` (201) | No (JWT + `GOVERNMENT_ADMIN`) |
| `GET` | `/api/v1/credits/issuances` | Every issuance nationally — the treasury's mint ledger | None | `List<CreditIssuanceResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `GET` | `/api/v1/credits/balance` | The calling farmer's ledger position **plus the season token ids to read `balanceOf` against** | None | `CreditBalanceResponseDTO` | No (JWT + `FARMER`) |
| `GET` | `/api/v1/credits/reconciliation?season=` | Issued vs redeemed vs held. Non-zero `discrepancyCredits` is an anomaly | None | `CreditReconciliationResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |

Validations in `SubsidyCreditService.issueCredits`, all in one `@Transactional`:
- duplicate `transaction_hash` → 409 (replay guard);
- the target user must hold `FARMER` **and** have a `wallet_address`;
- one issuance per (farmer, season) → 409, backed by a unique index;
- `token_id` must match the season's existing credit token, if one exists → 400.

Reconciliation maths in `CreditOversightService`: the identity
`issued = redeemed + heldByFarmers + heldBySellers` must hold, because credits
are created only by an issuance and destroyed only by a redemption burn. A gap
means credits moved wallet-to-wallet outside the marketplace. A seller is
`flagged` when they have redeemed ≥ 10 credits and their redemption rate exceeds
110% of what their completed orders earned (or they redeemed with no orders at all).

### Product Listings (`/api/v1/listings`)

Ownership comes from the JWT on **every** write — no endpoint accepts a seller id.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/listings` | The marketplace: **ACTIVE listings only**. Optional `?fertilizerType=`, `?isOrganic=`, `?isSubsidyEligible=`, `?sellerId=` | None | `List<ProductListingResponseDTO>` | No (JWT, any role) |
| `GET` | `/api/v1/listings/mine` | The calling seller's own listings, whatever their status | None | `List<ProductListingResponseDTO>` | No (JWT + `PRIVATE_AGRO_DEALER`/`ORGANIC_FERTILIZER_PRODUCER`) |
| `GET` | `/api/v1/listings/{listingId}` | One listing | None | `ProductListingResponseDTO` | No (JWT, any role) |
| `POST` | `/api/v1/listings` | Create. `isOrganic` is set from the caller's role | `ProductListingRequestDTO` | `ProductListingResponseDTO` (201) | No (JWT + seller roles) |
| `PUT` | `/api/v1/listings/{listingId}` | Replace one of the caller's own listings | `ProductListingRequestDTO` | `ProductListingResponseDTO` | No (JWT + seller roles) |
| `DELETE` | `/api/v1/listings/{listingId}` | Delete. **Refused (409) while any order is still riding on it** — pause instead | None | 204 | No (JWT + seller roles) |

### Marketplace Orders (`/api/v1/orders`)

**The role split here is the anti-fraud design, not an accident.** A seller can
reach `CONFIRMED` and no further; there is deliberately **no endpoint by which a
seller can complete an order or move a farmer's credits**.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/orders/quote?listingId=&quantityKg=&creditsUsed=` | Server-side costing so the live breakdown can be trusted | None | `OrderQuoteResponseDTO` | No (JWT + `FARMER`) |
| `POST` | `/api/v1/orders` | Place an order: reserves stock, **moves no tokens** | `PlaceOrderRequestDTO` | `MarketOrderResponseDTO` (201) | No (JWT + `FARMER`) |
| `GET` | `/api/v1/orders/me` | The calling farmer's orders, newest first | None | `List<MarketOrderResponseDTO>` | No (JWT + `FARMER`) |
| `GET` | `/api/v1/orders/seller` | Orders placed with the calling seller | None | `List<MarketOrderResponseDTO>` | No (JWT + seller roles) |
| `GET` | `/api/v1/orders` | Every order nationally | None | `List<MarketOrderResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `PATCH` | `/api/v1/orders/{orderId}/ready` | Seller marks goods ready → `CONFIRMED`. **The furthest a seller can move an order** | None | `MarketOrderResponseDTO` | No (JWT + seller roles) |
| `POST` | `/api/v1/orders/{orderId}/confirm` | **The farmer's confirmation** → `COMPLETED`. The only call that records a credit transfer | `ConfirmOrderRequestDTO` | `MarketOrderResponseDTO` | No (JWT + `FARMER`) |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Either party, while credits haven't moved. **Restores `available_kg`** | None | `MarketOrderResponseDTO` | No (JWT + `FARMER`/seller roles) |
| `POST` | `/api/v1/orders/{orderId}/dispute` | "This was not what I received" on a completed order — flags, reverses nothing (mirrors the distribution dispute) | None | `MarketOrderResponseDTO` | No (JWT + `FARMER`) |

Rules enforced in `MarketOrderService`, all server-side:
- `placeOrder` re-derives `creditsUsed` bounds and `cashAmountLkr` through `CreditMath` — the client's total is never trusted, **especially the organic 1.5×**;
- stock is reserved with a guarded `UPDATE … WHERE available_kg >= :amount`, so two farmers ordering the last 50kg cannot both succeed;
- credits are refused unless the listing is subsidy-eligible, the farmer has an issuance, and the amount is within their ledger balance;
- a farmer cannot order from their own listing;
- `confirm` **requires** `credit_transfer_hash` when `credits_used > 0` and **rejects** one when it is zero;
- duplicate `credit_transfer_hash` → 409.

### Redemption Claims (`/api/v1/redemption-claims`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/v1/redemption-claims` | File a claim, bounded by what completed orders earned less anything already claimed | `SubmitClaimRequestDTO` | `RedemptionClaimResponseDTO` (201) | No (JWT + seller roles) |
| `GET` | `/api/v1/redemption-claims/me` | The calling seller's claims | None | `List<RedemptionClaimResponseDTO>` | No (JWT + seller roles) |
| `GET` | `/api/v1/redemption-claims/pending` | Review queue (`SUBMITTED` + `APPROVED`), **oldest first** (FIFO) | None | `List<RedemptionClaimResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `GET` | `/api/v1/redemption-claims` | Every claim, newest first | None | `List<RedemptionClaimResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `PATCH` | `/api/v1/redemption-claims/{claimId}/review` | Approve or reject. `PAID` is **not** settable here | `ReviewClaimRequestDTO` | `RedemptionClaimResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`) |
| `POST` | `/api/v1/redemption-claims/{claimId}/settle` | The seller records the burn that settles an approved claim → `PAID` | `SettleClaimRequestDTO` | `RedemptionClaimResponseDTO` | No (JWT + seller roles) |

The claim ceiling (`sumOpenOrSettledCreditsBySeller`) counts every non-rejected
claim, so a seller cannot file the same credits twice before either is processed.
The response carries `creditTokenIds` so the admin's queue can read
`balanceOf` against the seller's wallet and verify custody before approving.

### Users (`/api/v1/users`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/users/me/profile` | The signed-in user's own profile: identity, editable details, plus wallet and area read-only for context | None | `ProfileResponseDTO` | No (JWT, any role) |
| `PUT` | `/api/v1/users/me/profile` | Replace the profile details (full name, address, contact number). PUT because the form submits all three together | `UpdateProfileRequestDTO` | `ProfileResponseDTO` | No (JWT, any role) |
| `GET` | `/api/v1/users/me/wallet` | The authenticated user's linked wallet address (null if never connected) | None | `WalletAddressResponseDTO` | No (JWT, any role) |
| `PATCH` | `/api/v1/users/me/wallet` | Link the connected wallet to the authenticated user. Lowercased before the uniqueness check; 409 if another account already holds it | `UpdateWalletAddressRequestDTO` | `WalletAddressResponseDTO` | No (JWT, any role) |

> Called automatically by the frontend's `useWalletAddressSync` hook (mounted in
> `app/(ui)/layout.tsx`) the first time thirdweb reports a connected address, so
> `users.wallet_address` is populated without a manual step. Re-sending the same
> address is a no-op; connecting a different wallet replaces the stored one.

### Areas (`/api/v1/areas`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/areas` | List all areas, ordered by district then area name | None | `List<AreaResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |

### Officers (`/api/v1/officers`)

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `GET` | `/api/v1/officers` | List users holding `AGRARIAN_SERVICE_OFFICER`, with their current area. Optional `?assigned=true\|false` filter; omitted returns all | None | `List<OfficerResponseDTO>` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |
| `POST` | `/api/v1/officers/assign` | Assign an officer to an area (sets `users.area_id` + `users.is_assigned`). Re-assigning moves the officer rather than failing | `AssignOfficerRequestDTO` | `OfficerResponseDTO` | No (JWT + `GOVERNMENT_ADMIN`/`SYSTEM_ADMIN`) |

### Fertilizer Requests (`/api/v1/fertilizer-requests`)

The farmer and officer identities come from the JWT principal, never the payload — neither `farmer_id` nor `reviewed_by_officer_id` is client-supplied.

| Method | Endpoint | Description | Request Body | Response | Public? |
|---|---|---|---|---|---|
| `POST` | `/api/v1/fertilizer-requests` | Raise a request for the authenticated farmer | `FertilizerRequestCreateDTO` | `FertilizerRequestResponseDTO` (201) | No (JWT + `FARMER`) |
| `GET` | `/api/v1/fertilizer-requests/me` | The authenticated farmer's own requests, newest first | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `FARMER`) |
| `GET` | `/api/v1/fertilizer-requests/pending` | Review queue: pending requests from farmers in the authenticated officer's area, **oldest first** (FIFO) | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `GET` | `/api/v1/fertilizer-requests/area` | Area history: every request from farmers in the officer's area, **newest first**, whoever reviewed it. Optional `?status=PENDING\|APPROVED\|REJECTED\|COLLECTED`; omitted returns all | None | `List<FertilizerRequestResponseDTO>` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |
| `PATCH` | `/api/v1/fertilizer-requests/{requestId}/review` | Approve or reject a pending request | `FertilizerRequestReviewDTO` | `FertilizerRequestResponseDTO` | No (JWT + `AGRARIAN_SERVICE_OFFICER`) |

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
   - `GlobalExceptionHandler.java` returns a uniform `ErrorResponse` for validation failures (`MethodArgumentNotValidException`, `ConstraintViolationException`), conflicts (`IllegalStateException`, `DataIntegrityViolationException`), bad input (`IllegalArgumentException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`), routing mistakes (`HttpRequestMethodNotSupportedException` → 405, `NoResourceFoundException` → 404), and auth failures (`AccessDeniedException`, `UsernameNotFoundException`, `InvalidTokenException`, `AccountBannedException`).
   - **500s never echo the exception message.** The cause is logged with its stack trace via SLF4J; the client gets a fixed sentence, because an unhandled exception's text can carry SQL and class names. Service-thrown 4xx messages *are* passed through — they are written for the user ("Sack … has already been used in an earlier handover").

4. **Authorization**: every controller method carries an explicit `@PreAuthorize`. Endpoints open to all signed-in users use `@PreAuthorize("isAuthenticated()")` rather than no annotation, so a missing one always reads as a bug. The acting user is always resolved from the JWT through `CurrentUserProvider` — no endpoint accepts an actor id in its payload.

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
