# WAL-0.7: Metrics → Prometheus + Grafana

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.7-metrics-grafana` |
| Depends on | WAL-0.3 |
| Estimate | 4 h |
| Target done | Sat 2026-12-05 |

## Goal
The app's JVM, HTTP and database pool metrics are visible on a Grafana dashboard.

## Scope
- `/actuator/prometheus` exposes JVM, HTTP server and HikariCP metrics, with an `application` tag.
- The `observability` profile gets Prometheus, which scrapes the app, and Grafana.
- Grafana is provisioned from files: a Prometheus datasource and one JVM/HTTP dashboard. You may ask me to
  scaffold the dashboard JSON. If I do, you still have to explain every panel.
- HTTP latency is recorded as a histogram, so p95 and p99 can be calculated.

## Acceptance criteria
1. Grafana shows heap usage, GC pauses, the HTTP request rate, p95 latency, and Hikari active and pending connections for the running app.
2. Should `/actuator/prometheus` be reachable without a token? `design.md` answers this and explains why, and the tests prove the behaviour.
3. The full stack (all profiles) uses less than 6 GB of memory (`docker stats`).

## Edge cases (answer them in [design.md](design.md) before you write code)
- Prometheus runs in a container, and the app may run on the host. How does Prometheus reach the app?
- High-cardinality labels: what happens to Prometheus if a URI tag contains wallet IDs (for example `/wallets/123`)?

## Definition of Done
- [ ] `design.md` approved (✅ in `review.md`) before the first code commit
- [ ] Acceptance criteria 1–3 pass
- [ ] Runbook section: which panel to check first when latency goes up
