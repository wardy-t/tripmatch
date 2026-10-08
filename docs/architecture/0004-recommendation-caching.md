# ADR 0004: Cache repeated recommendation results

## Status

Accepted

## Context

Recommendation requests load destinations, calculate multiple score components
and sort the results.

Different clients may submit identical or logically equivalent preferences.
Repeating the same database reads and calculations for every request creates
unnecessary work.

Caching also introduces risks including stale results, unbounded memory usage,
duplicate cache keys and inconsistent state across application instances.

## Decision

TripMatch caches completed recommendation response DTOs using Spring Cache and
Caffeine.

The cache has the following controls:

- A maximum of 500 recommendation entries
- Expiration five minutes after an entry is written
- A normalised cache key
- Synchronous calculation for identical concurrent cache misses
- Complete eviction after destination creation, update or deletion

The cache key includes:

- Normalised budget
- Climate
- Normalised maximum flight time
- Sorted, trimmed and lowercase interests
- Requested result limit

This means logically equivalent requests such as `1000` and `1000.00`, or
differently ordered interests, share the same entry.

## Consequences

### Positive

- Repeated recommendation requests avoid unnecessary database access.
- Repeated scoring and sorting work is avoided.
- Cache memory usage is explicitly bounded.
- Concurrent identical misses do not trigger duplicate calculations.
- Mutations invalidate results that could have become stale.
- PostgreSQL remains the source of truth.

### Negative

- The cache is local to one application instance.
- Restarting an instance clears its cache.
- Different application instances may temporarily contain different entries.
- Destination mutations clear all recommendation entries rather than only
  entries affected by the changed destination.

## Production evolution

A horizontally scaled deployment could use Redis or another distributed cache
if measurements demonstrate that shared caching provides sufficient value.

Production decisions should consider:

- Cache hit rate
- Eviction rate
- Recommendation latency
- Database load
- Memory usage
- Acceptable result staleness
- The cost of distributed-cache network calls

Caching should remain an optimisation rather than a source of truth.