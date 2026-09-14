# 0009 — Persisted activity lifecycle with derived time transitions

## Status

Accepted

## Context

Activities progress through invitations, CNU nominations, selection, delivery and payment closure. The synthetic application has no scheduler, but users must not see an invitation remain open after its nomination deadline or an accepted activity remain active after its end date.

## Decision

Persist the authoritative lifecycle status on the activity. The API performs event-driven transitions for invitation, selection, payment closure and cancellation. On every activity read it derives and persists time transitions: `INVITED` becomes `AWAITING_SELECTION` after the nomination deadline, and `ACCEPTED` becomes `CONCLUDED` after the activity end date. Nominations and cost lines are separate persisted child entities; closing requires at least one cost line and every line paid. Cancellation can override any status at an activity manager's request.

## Consequences

There is no background job in the synthetic slice, so time transitions occur on the next API read or lifecycle command. A scheduled reconciler is required before a production time-bound SLA can be claimed.
