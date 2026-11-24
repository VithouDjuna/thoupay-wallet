# ThouPay Wallet: Roadmap

> Epics and tickets for each phase are in [backlog](backlog/README.md).

## Phases
| Phase | Focus | Outcome |
|---|---|---|
| 0 | Setup | Repo, Docker Compose, Postgres, Keycloak, CI pipeline |
| 1 | Core wallet | Create wallet, top up, balance, ledger, history |
| 2 | Transfers | Transfers, fees, limits, idempotency, concurrency safety |
| 3 | Security and secrets | Keycloak roles, Vault, rate limiting, audit log |
| 4 | Events | Kafka, outbox pattern, notification service |
| 5 | Kubernetes | Helm charts, probes, resource limits, HPA, zero-downtime deploys |
| 6 | Observability | Grafana dashboards, Kibana logs, OpenTelemetry traces, alerts |
| 7 | Admin back office | Review queue, freeze, maker-checker refunds, reconciliation |
| 8 | Performance and incidents | k6 load tests, incident drills, postmortems |
| 9 | Public launch | Deploy live, demo accounts, README, LinkedIn post |


## Incident drills (production problems to practice)
- Heap memory leak causing `OutOfMemoryError`
- Pods `OOMKilled` (exit 137) from a mismatch between JVM heap and container limits
- CrashLoopBackOff from wrong liveness / readiness / startup probes
- Tomcat thread pool exhaustion from slow downstream calls without timeouts
- HikariCP connection pool exhaustion and slow queries (missing index, N+1)
- Deadlocks between concurrent transfers
- Kafka consumer lag causing delayed notifications
- CPU throttling from Kubernetes CPU limits
- Duplicate payment caused by client retries

Each drill ends with a postmortem in `docs/postmortems/` (timeline, root cause, fix, prevention).


## Portfolio deliverables (for LinkedIn / interviews)
- Live demo URL with ready-made demo accounts (customer and admin)
- Public read-only Grafana dashboard
- Architecture diagram in the README
- Architecture Decision Records (`docs/decisions/`)
- Load test results (k6 graphs and numbers)
- 2–3 incident postmortems written up as short articles
- A 2-minute demo video for the LinkedIn post
- Clean README: what it is, how to run it locally in one command, tech stack, key design decisions

