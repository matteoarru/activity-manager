# Java coding and Extreme Programming practices

The backend follows the EULECT coding principles while retaining Java and Spring conventions. This is an executable working agreement for the team, not a replacement for the product requirements.

## Code style

- Use UTF-8, LF line endings, final newlines and two-space indentation, as defined in `.editorconfig`.
- Keep classes small and capability-focused. API controllers translate HTTP concerns; application services express use cases; domain classes enforce invariants; infrastructure owns persistence and storage.
- Prefer explicit names, immutable records and constructor injection. Avoid clever abstractions, service locators and static mutable state.
- Use British English in user-facing text and domain comments. Keep protocol names and external field names unchanged where they are contractual.
- Validate at boundaries with Jakarta Validation, then enforce domain invariants in the domain layer. Never rely on UI validation alone.
- Do not log credentials, session identifiers, uploaded content or personal data. Return safe, actionable errors to callers.
- Use `java.time`, `BigDecimal` and explicit currencies for dates and money. Never use floating-point values for financial amounts.
- Keep SQL and object-storage operations behind infrastructure ports/repositories. Preserve deterministic ordering and idempotency at write boundaries.

## XP baseline for Java

Changes follow red-green-refactor:

1. Record the requirement and acceptance example before implementation.
2. Add a focused test that demonstrates the missing behaviour or defect.
3. Implement the smallest maintainable change until the focused test is green.
4. Refactor duplicated rules, misleading names and oversized methods while keeping the suite green.
5. Run the complete local gate (`npm run verify` or `npm run release:check`) before handover.

The test pyramid is deliberate: pure domain tests are fast and deterministic; API tests cover authentication, authorisation, validation and persistence boundaries; browser tests cover supported user journeys. Tests describe observable behaviour and use stable business identifiers, never database row order.

Definition of done also includes updated traceability, focused automated evidence, secure error handling, documentation and a clean diff. A release is built and tagged only from the exact source commit that passed the release gate.

## Review checklist

- Is the change mapped to a requirement and acceptance test?
- Does each class have one capability-level responsibility?
- Are access checks performed server-side for every write and upload?
- Are retries/idempotency and failure recovery explicit at persistence or external boundaries?
- Does the test cover both the successful journey and the relevant denial/validation path?
- Is the implementation readable without relying on comments to explain control flow?
