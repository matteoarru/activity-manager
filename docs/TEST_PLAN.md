# Test plan

## Evidence strategy

Tests are derived from observable acceptance criteria and risk:

- domain unit tests cover money, commitment and authorisation invariants;
- API integration tests cover authentication, role scope, validation, persistence, uploads and invitation recording;
- Playwright E2E tests cover the supported login and activity-setup journeys through the real frontend and API;
- the requirement matrix records which requirements are implemented, tested or still deferred.

## Coverage policy

`npm run test:coverage` runs JaCoCo in the Maven `coverage` profile and fails the build below 90% instruction or line coverage for every module that contains executable code. Branch coverage remains diagnostic; high-risk domain and permission paths receive focused denial tests in addition to the global gate.

The normal fast gate is `npm run test`. The browser gate is `npm run test:e2e`. `npm run release:check` runs both gates.

Additional user requests that change behaviour or quality expectations are recorded as immutable requirements before completion is reported. They must appear in `REQUIREMENTS.md`, `TRACEABILITY.md` and, where applicable, the E2E matrix.

## E2E policy

E2E tests use stable accessible labels and business codes, never row positions. They start the Vite frontend and a fresh synthetic Spring API using an in-memory H2 datasource, and use the installed Chrome channel. Each supported journey has an independently diagnosable test for its success and relevant recovery/denial path; generated activities and invitations are discarded when the suite finishes.

Implemented requirements have E2E evidence in [E2E_REQUIREMENTS_MATRIX.md](E2E_REQUIREMENTS_MATRIX.md). Deferred requirements remain explicitly marked as not testable until their implementation is accepted; they must not be reported as covered by placeholder tests.
