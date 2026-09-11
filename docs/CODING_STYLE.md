# Coding style and engineering practices

This repository follows the EULECT coding principles, adapted for a Java/Spring backend and a TypeScript/React frontend. The rules below are the team working agreement.

## General

- Keep modules small, explicit and capability-focused.
- Prefer clarity over clever abstraction. Extract shared code only after a repeated concept is proven.
- Keep source files UTF-8 with final newlines and follow the repository `.editorconfig`.
- Use British English for user-facing content and domain comments.
- Keep requirements, tests, traceability and operational documentation in the same change.
- Treat every additional user request that changes behaviour or quality as a requirement: assign an immutable ID before completion is claimed and link its implementation and evidence.

## Java and Spring

- Use two-space indentation, constructor injection, immutable records and explicit names.
- Controllers translate HTTP concerns; application services express use cases; domain classes enforce invariants; infrastructure owns persistence and storage.
- Validate untrusted input at the API boundary with Jakarta Validation, then enforce business invariants in the domain layer.
- Use `java.time`, `BigDecimal` and explicit currencies. Never use floating-point values for money.
- Keep SQL, file storage and external systems behind infrastructure boundaries.
- Enforce authentication, authorisation, ownership and idempotency server-side for every write.
- Never log passwords, session identifiers, uploaded content or unnecessary personal data.
- Return safe, actionable errors; never expose stack traces or local source paths.

## TypeScript and React

- Keep TypeScript strict and avoid `any`; narrow `unknown` at trust boundaries.
- Use function components and hooks. Keep data orchestration at feature boundaries and derive display values during render.
- Use stable business identifiers as React keys, never row positions.
- Prefer semantic HTML and accessible native controls before ARIA.
- Keep CSS tokens in variables and use content-driven responsive layouts.
- Keep `main.tsx` limited to application bootstrap. Put session bootstrap in `App`, feature navigation and data orchestration in a page-level component, and each independently understandable UI capability in `components/`.
- Components accept explicit typed props and emit actions through callbacks. Do not pass setters or broad page state through several layers when a feature-level callback expresses the intent.
- Preserve accessible names, labels, heading IDs and live-region roles during refactors; Playwright selectors based on these are part of the interface contract.
- Keep list sorting and pagination derivation close to the list component. Keep API calls and transient submission state close to the feature form or panel that owns them.

## Extreme Programming baseline

Work in small, reversible increments using red-green-refactor:

1. Record the requirement and observable acceptance example.
2. Add a focused test that fails for the missing behaviour.
3. Implement the smallest maintainable change until it is green.
4. Refactor duplication, misleading names and oversized methods while preserving behaviour.
5. Run `npm run verify`; run `npm run release:check` for release work.

Tests are behaviour evidence: pure domain rules are fast unit tests, API boundaries use integration tests, and supported journeys use browser tests. Use deterministic fixture codes rather than database order or incidental implementation details.

Component integration tests use the production Spring wiring at the boundary under test; they do not replace a repository, security component or infrastructure adapter with a mock and then claim cross-component evidence. Assign disposable datasource and storage paths, use unique business codes, and remove test-created rows and files in teardown. Refactor test setup into small helpers so each test reads as an acceptance example and failure output remains diagnosable.

## Definition of done

A change is done only when its requirement, focused test, implementation, refactoring, documentation, traceability and security evidence agree; the relevant quality gate passes from a clean build; and the diff contains no secrets, generated test output or unrelated changes.
