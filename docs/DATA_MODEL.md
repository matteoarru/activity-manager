# Data model and governance

Core records: Activity, Nomination, Engagement, ExpenseRevision, Commitment, AllocationJournal, Approval, Order, Invoice, DocumentVersion, AuditEvent and OutboxEvent. IDs are UUIDs; money is decimal plus ISO currency. A revision binds supplier, allocation, tax/commission, scope and approval/evidence versions.

Ledger buckets are disjoint: hold, authorised exposure and recognised consumption. Invoice recognition transfers covered exposure, rather than double charging. Evidence is private/versioned and awaits approved retention/legal-hold policy. Only synthetic data belongs in this repository.

The first persisted record is `activity`, with stable ID, course reference, dates, venue/time zone, status, funding regime and AM/support assignment. The synthetic H2 profile initialises it from `schema.sql`; PostgreSQL deployments use the Flyway migration in the infrastructure module.
