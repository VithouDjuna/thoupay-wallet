# ThouPay Wallet: Tech stack and architecture

> Owner: Architect (Claude). The stack is a company requirement, not a free choice. Decision history: [sprint-0-decisions.md](../decisions/sprint-0-decisions.md).

## Technology requirements (fixed, company-style)
| Area | Requirement |
|---|---|
| Language / framework | Java 21, Spring Boot 4 (latest 4.x), Gradle Kotlin DSL |
| Database | PostgreSQL 18 (exact image tag pinned; tests, compose and k8s use the same tag), Flyway migrations |
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


## Suggested services
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

