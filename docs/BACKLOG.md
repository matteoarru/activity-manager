# Backlog

| Item | Dependency/risk | Review |
| --- | --- | --- |
| PostgreSQL/Flyway ledger, row-lock integration race and Testcontainers | Required before any real spending claim | Increment 3 |
| OIDC/ABAC/provider/object-storage adapters and evidence lifecycle | External authority/contracts unknown | Before production |
| Outbox, notifications, dossier/restore proof | FR-014–021, DATA-003, SEC-002 | Increments 4–6 |
| Broaden Playwright accessibility and role-matrix journeys | Full profile matrix and WCAG regression evidence | Increment 2 |

## Checkpoint

2026-09-11: Activity setup, login and nomination journeys now have Playwright E2E evidence; JaCoCo enforces at least 90% instruction and line coverage per executable Maven module. Next: replace the in-memory ledger with PostgreSQL/Flyway plus a real concurrent integration test before exposing any allocation/order endpoint.

2026-09-11 (full specification): Added Flyway foundation schema, deterministic activities, JDBC repository, scoped `/api/v1/activities`, H2 synthetic persistence and Docker Compose PostgreSQL/object/mail services. Next: domain/API slices for nominations, engagements and commitments; PostgreSQL/Testcontainers race evidence remains outstanding.

See `docs/IMPLEMENTATION_STATUS.md` for the requirement-to-surface checkpoint and explicit deferred work.

Synthetic login uses fixture credentials only. Replace it with approved OIDC and MFA before a production deployment; never copy those fixtures into a real identity store.
