# Implementation status

This checkpoint implements the local foundation and a persisted activity read journey. It is not a production-readiness claim.

| Area | UI | API/persistence | Evidence | Status/blocker |
| --- | --- | --- | --- | --- |
| Synthetic profile login/logout | Login and role-aware workspace | Session auth, ten fixture profiles, `/api/me` | `SyntheticLoginIntegrationTest`, `frontend/e2e/activity-setup.spec.ts` | Implemented locally; production OIDC/MFA remains gated |
| Activity setup/read scope | Active activities dashboard, setup page and CNU invitation controls | H2 synthetic schema, Flyway PostgreSQL migration, JDBC repository, `/api/v1/activities`, curriculum upload and invitation endpoints | API integration tests, `frontend/e2e/activity-setup.spec.ts` | Partial; edit/archive and PostgreSQL Testcontainers remain |
| Quality gates | Playwright login/setup/invitation journeys and Java coverage gate | `npm run test:e2e`, `npm run test:coverage` (90% instruction/line minimum) | release commands | Implemented for current scope; broader requirement matrix remains |
| Money/commitment invariants | Existing demonstration data | Domain ledger | `CommitmentLedgerTest` | Partial; transactional PostgreSQL implementation remains |
| Nominations, engagements, attendance, catering | Not implemented | Not implemented | Planned in `BACKLOG.md` | Deferred |
| Procurement, approvals, orders, invoices, evidence, conversations, notifications, exports | Not implemented | Not implemented | Planned in `BACKLOG.md` | Deferred; external contracts unresolved |

The next checkpoint is the persisted nomination/engagement slice, followed by PostgreSQL row-lock integration evidence.
