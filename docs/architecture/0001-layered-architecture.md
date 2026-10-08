# ADR 0001: Use a layered application architecture

## Status

Accepted

## Context

TripMatch needs to expose HTTP endpoints, apply travel recommendation logic
and persist destination data in PostgreSQL.

Keeping these responsibilities inside controller classes would make the
application difficult to test and change. Recommendation logic should also
remain independent of HTTP and persistence concerns.

## Decision

TripMatch uses a layered architecture:

- Controllers handle HTTP requests and responses.
- Services coordinate business logic and transaction boundaries.
- Recommendation components calculate destination scores.
- Repositories provide database access through Spring Data JPA.
- Flyway owns database schema changes.

DTOs are used at the API boundary so persistence entities are not exposed
directly to clients.

## Consequences

### Positive

- Business logic can be tested without running a web server.
- Controllers remain focused on HTTP behaviour.
- Persistence implementation can evolve independently.
- Transaction boundaries are explicit.
- API contracts are separated from database entities.

### Negative

- More classes and mapping code are required.
- Small features can involve changes across several layers.