@AGENTS.md

Full architecture, DB schema, API table, and coding conventions live in AGENTS.md (imported above) — treat it as binding. This file only adds what AGENTS.md doesn't cover.

## Commands
- `chmod +x ./mvnw` — required once on a fresh clone (permission bit doesn't survive some checkouts)
- `./mvnw compile` — compile; run this before reporting any change done
- `./mvnw test` — run tests (only `BhumisaaraApplicationTests` context-load test exists today)
- `./mvnw spring-boot:run` — run locally (needs `.env.development`, already present locally with DB + JWT config)
- No CI configured yet — compile/test locally, don't assume a pipeline will catch it

## Keep in sync when changing endpoints
Adding, removing, or changing auth on a controller endpoint touches three places — update all three in the same change:
1. `SecurityConfig.java` `authorizeHttpRequests` (public vs authenticated)
2. The relevant API table in `AGENTS.md` §4
3. The `Public?` column in that same table

## Repo-specific rules
- `tokenId` is always `String`, never numeric (blockchain IDs like `"TK-0"`)
- New entities/DTOs follow the package + suffix conventions in AGENTS.md §7 — don't ask, just follow them
- Service methods: `@Transactional` for writes, `@Transactional(readOnly = true)` for reads
