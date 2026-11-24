# Backlog

One folder per epic, one sub-folder per ticket. Each ticket folder holds:

| File | Written by | Content |
|---|---|---|
| `ticket.md` | PO (Claude) | User story, acceptance criteria, edge cases, NFRs, DoD |
| `design.md` | Developer | Short design, written **before** any code |
| `review.md` | Tech lead (Claude) | Design review first, then the code review: `[severity] file:line: issue → why → hint` |
| `frontend.md` | Frontend (Claude) | Frontend integration for this ticket: what changed, why, API issues found |

**Big stories are split into sub-tickets.** The story folder keeps only `ticket.md` (goal, sub-ticket table,
story-level acceptance criteria), and each sub-ticket gets its own folder with the files above:

```
E0-platform/
└── WAL-0-platform-skeleton/
    ├── ticket.md                    ← story overview + sub-ticket status table
    ├── WAL-0.1-backend-skeleton/
    │   ├── ticket.md
    │   ├── design.md
    │   └── review.md
    └── WAL-0.2-ci-pipeline/ ...
```

One sub-ticket = one design = one branch (`feature/WAL-0.1-backend-skeleton`) = one PR.

A frontend change that doesn't belong to a ticket goes in `frontend-changes/FE-<n>-<slug>.md`.
Cross-cutting docs stay outside the backlog: `docs/decisions/` (ADRs), `docs/runbooks/`, `docs/postmortems/`.

## Epics
| Epic | Phase | Status | Tickets |
|---|---|---|---|
| [E0-platform](E0-platform/README.md) | 0: Setup | 🟡 In progress | WAL-0 |
| E1-core-wallet | 1: Wallet, top-up, balance, ledger, history | ⚪ Not started | |
| E2-transfers | 2: Transfers, fees, limits, idempotency, concurrency | ⚪ Not started | |
| E3-security | 3: Roles, Vault rotation, rate limits, audit | ⚪ Not started | |
| E4-events | 4: Kafka, outbox, notifications | ⚪ Not started | |
| E5-kubernetes | 5: Helm, probes, limits, HPA, zero-downtime | ⚪ Not started | |
| E6-observability | 6: Dashboards, logs, traces, alerts | ⚪ Not started | |
| E7-admin | 7: Review queue, freeze, maker-checker, reconciliation | ⚪ Not started | |
| E8-performance | 8: k6 load tests, drills | ⚪ Not started | |
| E9-launch | 9: Public demo | ⚪ Not started | |

Epic folders are created when their first ticket is given.
