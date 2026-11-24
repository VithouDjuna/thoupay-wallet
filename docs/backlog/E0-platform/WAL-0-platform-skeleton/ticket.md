# WAL-0: Platform skeleton (story)

| Field | Value |
|---|---|
| Type | Story (enabler), split into 7 sub-tickets |
| Epic | E0: Platform |
| Assignee | Developer (backend + infra) |
| Reporter | PO / Tech Lead (Claude) |
| Priority | Highest: blocks every other ticket |
| Estimate | ~30 h total |
| Target done | **Sun 2026-12-06** |

## User story
As the **ThouPay engineering team**, we need a runnable backend skeleton and a local platform
(database, identity, secrets, logs, metrics) **so that** every following ticket can meet the full
Definition of Done from day one, without anyone setting up infrastructure by hand.

There is **no wallet business logic** in this story. If you catch yourself writing a `Wallet` entity, stop.

## Sub-tickets (do them in this order)
| # | Sub-ticket | Depends on | Estimate | Target done | Status |
|---|---|---|---|---|---|
| [WAL-0.1](WAL-0.1-backend-skeleton/ticket.md) | Backend skeleton + module boundaries | – | 4 h | Thu 11-26 | 🟡 PR #1 in review |
| [WAL-0.2](WAL-0.2-ci-pipeline/ticket.md) | CI pipeline (GitHub Actions) | 0.1 | 2 h | Fri 11-27 | ⚪ To do |
| [WAL-0.3](WAL-0.3-compose-core/ticket.md) | Docker Compose: `core` profile | 0.1 | 4 h | Sat 11-28 | ⚪ To do |
| [WAL-0.4](WAL-0.4-vault-secrets-health/ticket.md) | Vault secrets + health groups | 0.3 | 5 h | Sun 11-29 | ⚪ To do |
| [WAL-0.5](WAL-0.5-keycloak-auth/ticket.md) | Keycloak realm + JWT auth + `/api/v1/me` | 0.3 | 6 h | Tue 12-01 | ⚪ To do |
| [WAL-0.6](WAL-0.6-logging-elk/ticket.md) | Structured logs + trace IDs → ELK | 0.3 | 5 h | Thu 12-03 | ⚪ To do |
| [WAL-0.7](WAL-0.7-metrics-grafana/ticket.md) | Metrics → Prometheus + Grafana | 0.3 | 4 h | Sat 12-05 | ⚪ To do |

Sun 12-06 is a buffer day for the end-to-end check below. I also integrate the frontend that day.

## How each sub-ticket flows
1. You implement it on its own branch, `feature/WAL-0.<n>-<slug>`, and open one PR.
2. The PR description has a **Decisions** section: one short answer per "Edge cases" question in the sub-ticket, plus at least one risk.
3. I review decisions and code together in `review.md` in the sub-ticket folder. You fix, then squash-merge.
4. You update the status in the table above.

One sub-ticket = one branch = one PR. Don't mix sub-tickets in one PR.

## Story-level acceptance criteria (checked on 12-06, after all sub-tickets are merged)
1. From a clean clone, the documented commands bring up **all** profiles, and every container is healthy within 3 minutes.
2. A user logs in to the customer web app through Keycloak and sees their `/api/v1/me` data. I build the frontend part.
3. That request's `traceId` finds all of its log lines in Kibana, and the request shows up in the Grafana HTTP panel.
4. The full stack (`docker stats`) stays under **6 GB** of memory.
5. With only `docs/runbooks/local-platform.md`, someone who has never seen the repo can start, stop and reset the platform.

## Rules for every sub-ticket
- Everything is reproducible. Don't configure anything by hand in the Keycloak, Grafana or Kibana UIs: if you clicked it, it isn't done.
- No secrets in git. Until WAL-0.4 moves them to Vault, local-only values go in an ignored `.env`.
- Each sub-ticket adds its own section to `docs/runbooks/local-platform.md`.
