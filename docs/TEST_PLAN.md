# Test plan

## Deriving evidence

For each requirement: identify acceptance criteria, user/system risks, failure modes and the lowest useful test level. Map the result in `TRACEABILITY.md`.

| Test level | Purpose | Typical evidence |
| --- | --- | --- |
| Unit | Pure rules and transformations | Fast deterministic tests; target 100% for core domain logic |
| Component | UI/service behaviour at a boundary | Loading, empty, error, accessibility and validation states |
| Integration | Real contracts across introduced boundaries | Resettable sandbox services and versioned fixtures |
| E2E | Supported user/system journeys | Independently diagnosable browser or API journeys |
| Release | Deployable product confidence | Security audit, checks, coverage, build, packaging and smoke tests |

## Coverage policy

Set enforceable thresholds separately for statements, branches, functions and lines. Explain the thresholds based on the application type; do not lower them merely to hide an untested behaviour. E2E evidence complements, never replaces, unit and component coverage.

## Future integration strategy

When APIs, identity, persistence, third-party systems, scheduled imports or queues are introduced, add sandbox integration tests in the same change. Test contracts, validation, authentication/authorisation, timeouts, retry/idempotency, partial failures, migrations and operator-visible diagnostics.
