# E2E requirements matrix

This matrix prevents a passing browser suite from being mistaken for coverage of functionality that has not been implemented. A requirement is “covered” only when an independently diagnosable Playwright journey exercises its observable acceptance criteria.

| Requirement | Current scope | E2E evidence | Status |
| --- | --- | --- | --- |
| `FR-001` | Create and list an activity with dates, location, funding and participant target | `frontend/e2e/activity-setup.spec.ts`: AM creates an activity and sees its business code on the dashboard | Covered for the implemented activity-setup slice |
| `FR-002` | Nomination invitation recording to CNUs | `frontend/e2e/activity-setup.spec.ts`: selects a CNU and sends a nomination invitation from the created activity | Covered for the implemented invitation slice |
| `SEC-003` | Synthetic sign-in, role session and invalid credentials | `frontend/e2e/activity-setup.spec.ts`: successful AM sign-in and invalid-credential denial | Covered for synthetic mode |
| `FR-022` | All identified profiles authenticate, receive scoped sessions and sign out | API integration test covers all fixtures; browser flow covers representative AM login | Partial E2E; full profile matrix is API evidence |
| `FR-003`–`FR-021` | Not implemented in the current vertical slice | No placeholder E2E tests; tracked in `REQUIREMENTS.md` and `BACKLOG.md` | Deferred until implementation |
| `DATA-001`–`DATA-003` | Money/ledger partial; evidence/export deferred | Domain/API tests only for current slice | Partial/deferred |
| `SEC-001`–`SEC-004` | Synthetic role and assignment checks implemented; production controls deferred | API denial/session tests; browser representative login | Partial |
| `NFR-001`–`NFR-004` | Build, test, accessibility and recovery targets at different maturity | Build, coverage and current E2E gates | Partial/deferred |
| `NFR-005` | Every accepted behavioural requirement is mapped to test evidence; no deferred feature is represented by a placeholder E2E test | This matrix, `TRACEABILITY.md`, Playwright suite | Covered for current scope |
| `NFR-006` | Backend unit coverage is at least 90% for instructions and lines | `npm run test:coverage` | Covered |

When a deferred requirement is implemented, its acceptance example, API/domain tests and a dedicated E2E row must be added in the same change.
