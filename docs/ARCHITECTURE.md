# Architecture

## Context and boundaries

React browser UI calls a Spring API through a typed request boundary. The UI keeps activity/profile rules in a small domain module and renders capability-focused workspace flows. The Spring API owns authorisation and coordinates PostgreSQL/object storage/outbox worker. ABAC remains authoritative and is manual-reference/export-only here; provider/email adapters are synthetic.

## Components and data flow

`domain` owns money/allocation/state rules without Spring. `application` coordinates ports and use-case guards. `infrastructure` owns JDBC/Flyway/outbox/adapters. `api` is HTTP and `worker` is asynchronous. Controllers are boundary adapters with constructor injection and named validation/authorisation decisions. A protected local-envelope ledger locks/rereads commitment state; an advisory import cannot authorise orders.

## Quality attributes

BigDecimal/numeric money, immutable revisions, idempotency keys and disjoint hold/exposure/consumption buckets protect finance. Role-plus-attribute checks constrain access. See ADRs 0002–0008.
