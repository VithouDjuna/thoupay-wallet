# ThouPay Wallet

A web-based digital wallet (top-up, transfers, QR payments) with an admin back office: a production-grade
Spring Boot backend built as senior-engineer training, and a live demo for the portfolio.
Backend and infra: developer. Frontend: Claude.

## Where things are
| Document | Content |
|---|---|
| [docs/product/requirements.md](docs/product/requirements.md) | Overview, roles, features, business rules, NFRs, out of scope |
| [docs/architecture/tech-stack.md](docs/architecture/tech-stack.md) | Fixed tech stack, module layout |
| [docs/roadmap.md](docs/roadmap.md) | Phases, incident drill list, portfolio deliverables |
| [docs/backlog/README.md](docs/backlog/README.md) | Epics and tickets (ticket, design, review, frontend notes) |
| [docs/decisions/](docs/decisions/) | Sprint 0 decisions and ADRs |
| [docs/learning/](docs/learning/) | Learning notes: `wal-0.1-backend-skeleton-explained.md` (ticket walkthrough), `flyway-basics.md`, `testcontainers-basics.md` |
| [docs/runbooks/](docs/runbooks/), [docs/postmortems/](docs/postmortems/) | Operations notes, incident write-ups |
| [discussion.md](discussion.md) | Current open discussion with the tech lead |

---

## Project status (team memory, updated every session)
> Claude reads this at the start of every session. Chat history does not carry over between sessions,
> so this file and `discussion.md` are the only memory that does.

**Last updated:** 2026-11-24 (Sprint 0 session 2)

### Current sprint and ticket
- **Sprint 0** discussion is closed. All decisions are in `docs/decisions/sprint-0-decisions.md`.
- **Current ticket:** WAL-0, Platform skeleton (`docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`), given 2026-11-24.
  - Split into 7 sub-tickets (WAL-0.1 … WAL-0.7), each with its own design, review and PR. Story target: Sun 2026-12-06.
  - **Now:** WAL-0.1 Backend skeleton. Design due Wed 2026-11-25 in `docs/backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/design.md`.
  - Status: waiting for the WAL-0.1 design. No code before the design review. Sub-ticket status table: `docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`.
- **Next:** WAL-1 (wallet creation + balance), then WAL-2 (ledger). Milestone 1 = Phases 0–2 by 2026-12-31.

### Open questions
- None.

### Housekeeping still open (developer's actions)
- First push not done: the GitHub remote is empty. Rename `master` → `main` before the first push (decision D4).
- The old parent repo `C:\Users\thenc\Project` still has `origin` pointing at `thoupay-wallet.git`. Never push from there.
- `C:\Users\thenc\Project\project-stepup` still exists. The developer deletes it (Claude won't).
- Set `JAVA_HOME` to JDK 21 so `jcmd` / `jstack` work, before the first incident drill.

### Developer profile and weak areas
- 3+ years of Spring Boot. Has used Redis, Keycloak and Vault, but only as already set up by DevOps, without deep understanding.
- Has seen pod restarts at work, but someone else found the root cause (leak, heap growth, busy threads).
- Self-assessed weak in all of: concurrency, transactions, k8s, observability, security, system design. Has never used Testcontainers.
- Chose to defer the architecture decisions (balance model, PII encryption, Kafka rationale, SLO). They come back as
  required inputs to ticket designs (see "Deferred" in `docs/decisions/sprint-0-decisions.md`).
- Took the tech lead's recommendation on currency (USD only) without debate. That's fine here, but watch for
  a pattern of accepting defaults without reasoning.

### Known risks
- WSL2 has no `.wslconfig`, so it gets about 8 GB. The full stack may get OOM-killed (WAL-0 caps it at 6 GB).

### Open review comments
- None yet.

### Completed incidents
- None yet.

### Skill assessments
- None yet (the first one comes after about 3 tickets).
