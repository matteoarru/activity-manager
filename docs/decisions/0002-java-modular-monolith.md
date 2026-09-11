# ADR-0002: Java modular monolith

- Status: Accepted
- Date: 2026-09-11

Use Java 25, Spring Boot and a Maven reactor with domain/application/infrastructure/API/worker modules. This retains an explicit financial transaction boundary while allowing separate API and worker processes.
