# CEPOL Event Operations Service

## Purpose

An API-first operational workspace for CEPOL onsite activities. It reduces re-entry and makes approval, commitment, supplier, invoice and evidence trails inspectable without putting Finance into routine booking operations.

## Scope

The first synthetic vertical slice covers activity/nomination provenance, an IA-prepared priced request, protected commitment hold, delegated AM approval, provider instruction, invoice matching and Finance registration hand-off. It excludes live booking/payment, ABAC posting, production OIDC, real supplier communication, Moodle/ERP database sharing and auditor accounts.

## Success measures

Demonstrate a traceable end-to-end journey, prevent overspending in a protected local envelope, constrain Finance to registration hand-off, and give accessible status/error feedback. Pilot measures: handling/rework time, missing evidence, invoice first-pass match and reconciliation errors.

## Constraints and assumptions

Java 25/Spring Boot 4.1 and PostgreSQL are targets; H2 is only a fast local test profile and never evidence of cross-instance financial safety. Money is decimal/currency based. The IGN informs configurable policy context, not a production-compliance claim. No confidential documents, credentials or real personal data are committed.
