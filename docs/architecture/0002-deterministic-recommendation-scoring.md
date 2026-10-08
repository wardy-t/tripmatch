# ADR 0002: Use deterministic recommendation scoring

## Status

Accepted

## Context

TripMatch must rank destinations using traveller preferences including budget,
climate, interests and maximum flight time.

A machine-learning model would require representative behavioural or booking
data that the project does not currently possess. Introducing one without
appropriate data would make results harder to explain without improving their
quality.

## Decision

TripMatch uses a deterministic weighted scoring model with a maximum score of
100:

| Category | Maximum score |
|---|---:|
| Budget compatibility | 35 |
| Climate match | 25 |
| Shared interests | 30 |
| Flight-time compatibility | 10 |

The scoring implementation is isolated from the web and persistence layers.

Equal scores are ordered by average cost and then city name, producing stable
results across repeated requests.

## Consequences

### Positive

- Recommendation results are explainable.
- Identical inputs produce identical results.
- Individual scoring categories can be unit-tested.
- Weighting changes can be reviewed and version controlled.
- No training data or model infrastructure is required.

### Negative

- Weights are manually selected.
- The model does not learn from user behaviour.
- Preference interactions are limited to explicitly implemented rules.

## Future considerations

If TripMatch later collects sufficient anonymised interaction data, an
experimentally validated ranking model could complement or replace parts of
the deterministic scorer.

The deterministic score should remain available as a baseline for comparison,
explainability and fallback behaviour.