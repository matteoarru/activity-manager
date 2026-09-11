# Repository guidance

## Operating rules

- Keep the product scope in `docs/PRODUCT_BRIEF.md` current. Do not add workflows or integrations outside it without an accepted requirement and architecture decision.
- Record accepted requirements in `docs/REQUIREMENTS.md` before implementation. Use immutable IDs: `FR-###`, `NFR-###`, `SEC-###`, `DATA-###`.
- Update `docs/TRACEABILITY.md` whenever a requirement, implementation, test or status changes.
- Record a new ADR for a meaningful architecture, security, data-governance, deployment or dependency decision. Supersede; do not rewrite historical ADRs.
- Keep generated artefacts out of source control unless the project explicitly treats them as source.

## Verification and release

- Define stack-specific `verify`, `test:coverage`, `test:e2e`, `build` and `release:check` commands and expose the same commands through VS Code tasks and CI.
- `release:check` must include dependency/security checks, formatting/lint/type checks appropriate to the stack, automated tests, coverage, production build, E2E and deployment/package validation where applicable.
- Before creating or pushing a minor or major release: commit the release version, run `release:check` on that exact commit, record evidence, then tag and push only if it passes.
- Use semantic versioning and keep package/application version, changelog, release artefacts and tag aligned.

## Test derivation

- Derive tests from requirement acceptance criteria and risks, not from implementation internals alone.
- Map every supported user or system journey to independently diagnosable automated evidence at the appropriate level.
- Pure domain rules should target 100% coverage. Set enforceable global thresholds separately for statements, branches, functions and lines; explain them in `docs/TEST_PLAN.md`.
- Activate sandbox integration testing when the product introduces APIs, persistence, identity, external systems, scheduled jobs or message queues.
