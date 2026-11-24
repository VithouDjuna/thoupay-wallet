# Sprint 0 decisions

> Decided 2026-11-24 in the Sprint 0 discussion. Later decisions get their own ADR in this folder (`ADR-<nnn>-<slug>.md`).

### Project and repo
| # | Topic | Decision |
|---|---|---|
| D1 | Name | **ThouPay Wallet** (`thoupay-wallet`), GitHub `VithouDjuna/thoupay-wallet` |
| D1a | Location | `C:\Users\thenc\thoupay-wallet`, a standalone git repo isolated from other projects |
| D2 | Build tool | Gradle, **Groovy DSL** (changed from Kotlin DSL 2026-11-24 after the developer pushed back: a Java team reads Groovy without learning Kotlin syntax) |
| D3 | Layout | Monorepo: `backend/` (developer), `frontend/` (Claude), `infra/`, `docs/{product,architecture,backlog,decisions,runbooks,postmortems}` + `docs/roadmap.md` |
| D4 | Branching | Trunk-based (`main`), `feature/WAL-<n>-<slug>` (sub-tickets: `feature/WAL-<n>.<m>-<slug>`), squash-merge, Conventional Commits |
| D5 | Language | English for tickets, reviews and docs |

### Way of working
| # | Topic | Decision |
|---|---|---|
| D6 | Definition of Done | Option A: a minimal platform (Postgres, Keycloak, Vault, ELK, Prometheus, Grafana) in Sprint 0 (WAL-0); full DoD from ticket 1 |
| D7 | Backend reviews | `review.md` in the ticket folder (design review, then code review), format `[severity] file:line: issue → why → hint` |
| D8 | Frontend changes | A branch per change + `frontend.md` in the ticket folder, or `docs/backlog/frontend-changes/FE-<n>-<slug>.md` if not tied to a ticket (what, why, what to learn). The developer merges |
| D9 | Feedback style | Direct. Claude says so when work looks junior |
| D10 | Discussion log | `discussion.md` is rewritten after each conversation to show the current state |
| D10a | Backlog layout | `docs/backlog/E<n>-<epic>/WAL-<n>-<slug>/` with `ticket.md`, `design.md`, `review.md`, `frontend.md`. A big story is split into sub-ticket folders `WAL-<n>.<m>-<slug>/` with the same files, and one PR each. Index in `docs/backlog/README.md` (2026-11-24) |

### Time
| # | Topic | Decision |
|---|---|---|
| D11 | Capacity | 2 h every weekday, up to 6 h per weekend day (about 22 h/week) |
| D12 | Milestone 1 | 2026-12-31: Phases 0–2 done (platform skeleton, wallet, ledger, safe transfers, first drill) |
| D13 | Public launch | Around May 2027 |

### Developer background
| # | Topic | Answer |
|---|---|---|
| D14 | Used in production | Redis, Keycloak, Vault, only as set up by DevOps |
| D15 | Incidents seen | Pod restarts; a tech lead found the root cause |
| D16 | Weak areas | Concurrency, transactions, k8s, observability, security, system design |
| D17 | Testcontainers | Never used |

### Machine and tools
| # | Topic | Decision |
|---|---|---|
| D18 | Machine | Laptop: 16 GB RAM, 12 CPUs, Windows 11 + WSL2 + Docker |
| D19 | Docker memory | No `.wslconfig` (~8 GB default). Known OOM risk |
| D20 | Compose | Profiles `core`, `observability`, `messaging`; Elasticsearch heap capped at 1 GB |
| D21 | JDK | JDK 21 via IntelliJ (`~/.jdks/openjdk-21.0.2`); set `JAVA_HOME` before the first drill |
| D22 | Tools later | `k3d` + Helm in Phase 5, `k6` in Phase 8 |

### Frontend
| # | Topic | Decision |
|---|---|---|
| D23 | Apps | `frontend/customer` and `frontend/admin`, separate Keycloak clients |
| D24 | Stack | React + TS + Vite, TanStack Query, React Router, Tailwind + shadcn/ui, `openapi-typescript`, `oidc-client-ts` (PKCE) |
| D25 | Brand | ThouPay; Claude chooses the look |
| D26 | API contract | RFC 9457 `problem+json`, pagination on every list, `Idempotency-Key` on every money-moving POST, money as string decimals, ISO-8601 UTC |

### Architecture
| # | Topic | Decision |
|---|---|---|
| D27 | Auth timing | Keycloak + basic roles in Phase 0/1; fine-grained roles, Vault rotation, rate limits in Phase 3 |
| D28 | Early drills | JVM/DB-level on Docker Compose; pod-level drills after Phase 5 |
| D29 | Currency | **USD only (Q2 = A, 2026-11-24).** A `currency` column (ISO 4217, always `USD`) is stored wherever money is stored |

### Deferred (required inputs to later designs; "I don't know yet" won't pass review)
| Topic | Comes back in |
|---|---|
| Balance model (derived vs. stored) and locking | WAL-2 design (ledger), becomes ADR-001 |
| Why Kafka + outbox instead of a DB queue | Phase 4 design |
| PII encryption (fields, keys, searching by phone) | The first ticket that stores phone/KYC data |
| SLO (99.9% isn't possible on one node) and the 200 TPS definition | Phase 5 and Phase 8 |
| Public demo abuse (open sign-up or demo accounts) | Phase 9 |
| Hosting, domain, SMTP, production logging | Phase 9 |
