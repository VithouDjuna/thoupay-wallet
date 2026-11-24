# Digital Wallet Platform: Project Idea

## 1. Overview
A web-based digital wallet where users can hold a balance, top up, transfer money to other users,
and pay with QR codes. An admin back office lets operations staff review risky transactions,
freeze accounts, issue refunds, and audit every money movement.

**Goal of this project:** build a production-grade backend that shows senior-level skill in
business logic, correctness, security, infrastructure, observability, and incident handling,
and is publicly accessible as a live demo for my portfolio (LinkedIn / interviews).

**Who builds what**
- Backend, API design, infrastructure: me
- Web frontend (customer site and admin back office): Claude, integrating with my API

---

## 2. Users and roles
| Role | Description |
|---|---|
| Customer | Registers, verifies identity (KYC), manages wallet, sends and receives money |
| Merchant | Receives QR payments, sees settlement reports |
| Ops Admin | Reviews flagged transactions, freezes/unfreezes accounts, handles disputes |
| Finance Admin | Issues refunds, views ledger and reconciliation reports |
| Auditor (read-only) | Views audit logs and reports, cannot change anything |

---

## 3. Features

### Customer web app
- Sign up / log in (Keycloak), with optional 2FA
- KYC levels: unverified → basic → full (each level has different limits)
- Wallet dashboard: balance, wallet ID, daily/monthly limits used
- Top up from a (simulated) bank or card
- Transfer to another wallet (by wallet ID or phone), with fee quote and confirmation
- Pay merchant by QR code
- Transaction history with filters, pagination, and CSV export
- Notifications (email / in-app) for every money movement

### Admin back office
- Search users, wallets, and transactions
- Review queue for flagged transactions (approve / reject)
- Freeze / unfreeze wallets with a reason
- Refunds and reversals (require approval from a second admin: maker-checker)
- Ledger view and daily reconciliation report
- Full audit log: who did what, when, from where

---

## 4. Core business rules
- **Double-entry ledger:** every money movement creates balanced debit and credit entries.
  Balance is derived from or reconciled against the ledger. Money is never created or lost.
- **Money type:** use `BigDecimal` / `NUMERIC(19,4)`, never `double`.
- **Idempotency:** every write request (top-up, transfer, payment) requires an `Idempotency-Key`.
  The same key returns the same result and never charges twice.
- **Limits by KYC level:** per-transaction, daily, and monthly limits. A transfer that exceeds a limit is rejected.
- **Fees:** configurable fee rules (flat, percentage, free under a threshold).
- **Risk checks:** transfers above a threshold, or many transfers in a short time, go to manual review.
- **Transaction states:** `PENDING → COMPLETED | FAILED | REVERSED`, plus `ON_HOLD` for review.
  Only valid state transitions are allowed.
- **Frozen wallets** cannot send money. Receiving is allowed or blocked based on the freeze reason.
- **Refunds** create new reversing ledger entries. Never update or delete old entries.
- **Concurrency:** two simultaneous transfers from the same wallet must never overdraw it.

---

## 5. Technology requirements (fixed, company-style)
| Area | Requirement |
|---|---|
| Language / framework | Java 21, Spring Boot 3 |
| Database | PostgreSQL, Flyway migrations |
| Cache / locks / rate limit | Redis |
| Messaging | Kafka (transaction events, notifications), outbox pattern |
| Authentication | Keycloak (OIDC / JWT), role-based access |
| Secrets | HashiCorp Vault (DB credentials, API keys, rotation) |
| Containers / orchestration | Docker, Kubernetes (k3s for the public deployment) |
| Logs | ELK stack, viewed in Kibana (structured JSON logs with trace IDs) |
| Tracing | OpenTelemetry |
| Metrics / dashboards | Prometheus + Grafana (JVM, HTTP, DB pool, business metrics) |
| K8s visibility | Headlamp, Coroot |
| CI/CD | GitHub Actions: build, test, image, deploy |
| Testing | JUnit 5, Testcontainers, k6 for load testing |
| API docs | OpenAPI (springdoc) |

---

## 6. Non-functional requirements
- **Latency:** p95 under 200 ms for reads and under 500 ms for transfers
- **Availability:** 99.9% target, zero-downtime deployments
- **Correctness:** zero balance mismatches in the daily reconciliation
- **Security:** OWASP Top 10, no secrets in code or images, PII encrypted at rest, rate limiting on auth and transfers
- **Auditability:** every admin action and money movement is logged and traceable end to end
- **Scalability:** a transfer flow that is proven with load tests (target: 200+ transfers/sec on a small cluster)

---

## 7. Suggested services
Start as a **modular monolith**. Split into services only when there is a clear reason.

```
wallet-platform/
├── identity   ← user profile, KYC level (auth itself is Keycloak)
├── wallet     ← wallets, balances, limits, freeze
├── ledger     ← double-entry entries, reconciliation
├── transfer   ← top-up, transfer, QR payment, fees, idempotency
├── risk       ← rules, review queue
├── admin      ← back-office APIs, maker-checker, audit
└── notification ← consumes Kafka events, sends email / in-app
```

---

## 8. Roadmap
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

---

## 9. Incident drills (production problems to practice)
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

---

## 10. Portfolio deliverables (for LinkedIn / interviews)
- Live demo URL with ready-made demo accounts (customer and admin)
- Public read-only Grafana dashboard
- Architecture diagram in the README
- Architecture Decision Records (`docs/decisions/`)
- Load test results (k6 graphs and numbers)
- 2–3 incident postmortems written up as short articles
- A 2-minute demo video for the LinkedIn post
- Clean README: what it is, how to run it locally in one command, tech stack, key design decisions

---

## 11. Out of scope (for now)
- Real bank or card integrations (simulated only)
- Native mobile apps (the web app is responsive)
- Multi-currency and FX (a possible later phase)