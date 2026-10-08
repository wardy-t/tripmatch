# ADR 0003: Protect expensive endpoints with bounded traffic

## Status

Accepted

## Context

Recommendation requests perform more business logic than basic destination
retrieval and may inspect multiple destinations to calculate and rank scores.

Unbounded traffic could exhaust application or database capacity and degrade
service for every client.

TripMatch also needs a repeatable way to validate behaviour under concurrent
requests.

## Decision

TripMatch applies the following traffic controls:

- Destination collection responses have a default page size of 20.
- Requested page sizes are capped at 100.
- The recommendation endpoint uses a token-bucket rate limiter.
- The limiter accepts a burst of 30 requests and replenishes those tokens
  over 60 seconds.
- Rejected requests return `429 Too Many Requests`.
- Responses include limit, remaining-capacity and retry headers.
- A k6 test sends concurrent traffic and verifies both accepted and
  rate-limited responses.
- CI executes the k6 test against the containerised application.

Caching has not been introduced because the current project has no production
evidence showing that database reads are a bottleneck. Adding caching without
that evidence would introduce invalidation and stale-data concerns.

## Consequences

### Positive

- Expensive requests cannot grow without an explicit bound.
- Clients receive clear retry guidance.
- Traffic behaviour is covered by automated tests.
- Performance thresholds can detect significant regressions.
- Pagination prevents unbounded collection responses.

### Negative

- The rate-limit state exists only inside one application instance.
- Restarting the application resets the available capacity.
- The limit is global rather than associated with an authenticated client.
- The configured limit is demonstrative rather than derived from production
  traffic.

## Production evolution

A horizontally scaled deployment should apply rate limits through an API
gateway or a shared store such as Redis.

Limits should be segmented by an authenticated client, API key or partner and
selected using measured capacity, service-level objectives and expected usage.

Production monitoring should track request rate, rejected requests, latency,
database utilisation and saturation before changing limits or introducing
caching.

## Baseline result

A local Docker-based k6 run issued 40 requests through 10 concurrent virtual
users:

- 30 requests were accepted.
- 10 requests received controlled `429` responses.
- All functional checks passed.
- No unexpected HTTP failures occurred.
- The 95th-percentile request duration was approximately 756 milliseconds.

This local result validates behaviour but is not a production capacity
guarantee.