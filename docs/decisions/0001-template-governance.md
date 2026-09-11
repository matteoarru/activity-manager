# ADR-0001: Requirements-driven documentation governance

- Status: Accepted
- Date: 2026-09-11

## Context

Projects need a lightweight, stack-independent way to prevent requirements, design, documentation, tests and release claims from diverging.

## Decision

Use stable requirement IDs, traceability to implementation and automated evidence, ADRs for consequential decisions, shared VS Code/CI command names, and a release gate run on the exact versioned commit.

## Consequences

Documentation is maintained as part of delivery. Changes that affect product behaviour, data, architecture or deployment also update their relevant documents and tests.
