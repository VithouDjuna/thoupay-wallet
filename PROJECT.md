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
| [docs/backlog/README.md](docs/backlog/README.md) | Epics and tickets (ticket, review, frontend notes) |
| [docs/decisions/](docs/decisions/) | Sprint 0 decisions and ADRs |
| [docs/learning/](docs/learning/) | Learning notes: `wal-0.1-backend-skeleton-explained.md` (ticket walkthrough), `flyway-basics.md`, `testcontainers-basics.md` |
| [docs/runbooks/](docs/runbooks/), [docs/postmortems/](docs/postmortems/) | Operations notes, incident write-ups |
| [discussion.md](discussion.md) | Current open discussion with the tech lead |

---

## Project status (team memory, updated every session)
> Claude reads this at the start of every session. Chat history does not carry over between sessions,
> so this file and `discussion.md` are the only memory that does.

**Last updated:** 2026-11-24 (Sprint 0 session 3)

### Current sprint and ticket
- **Sprint 0** discussion is closed. All decisions are in `docs/decisions/sprint-0-decisions.md`.
- **Current ticket:** WAL-0, Platform skeleton (`docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`), given 2026-11-24.
  - Split into 7 sub-tickets (WAL-0.1 … WAL-0.7), each with its own review and PR (no design step, D30). Story target: Sun 2026-12-06.
  - **Now:** WAL-0.1 Backend skeleton. PR #1 open, code review round 4 = **request changes** (2026-11-24). Fixes due Thu 2026-11-26. Review: `WAL-0.1-backend-skeleton/review.md`.
  - Round 4: developer asked for re-review but pushed nothing (PR head still dece471); reviewed the local working tree. 6/10 round-3 items fixed.
    Main blocker remains in a new form: `PostgresTestConfiguration` is never `@Import`ed and `spring.profiles.active: local` is in the main
    `application.yml`, so the IT still runs against local `thoupaydb` (verified by running the build). Their pasted "T5" error was actually
    "port 5432 refused", which proves it.
  - Learning notes written for the developer (who is new to Flyway and Testcontainers): `docs/learning/`. They learn best from
    friendly, analogy-based, step-by-step explanations with hands-on exercises.
  - Status: waiting for fixes pushed to PR #1. Sub-ticket status table: `docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`.
- **Next:** WAL-1 (wallet creation + balance), then WAL-2 (ledger). Milestone 1 = Phases 0–2 by 2026-12-31.

### Open questions
- None.

### Housekeeping still open (developer's actions)
- ✅ Done 2026-11-24: `master` renamed to `main` and pushed. GitHub default branch is `main`. The stray remote `master`
  (easy-common shop code pushed from the old repo on 2026-11-23) was deleted. The old folder `C:\Users\thenc\Project`
  (including `project-stepup`) was deleted by the developer, so the wrong-remote risk is gone.
- `PROJECT.md` has uncommitted team-memory updates. Commit them on the feature branch, separate from the code commit.
- Set `JAVA_HOME` to JDK 21 so `jcmd` / `jstack` work, before the first incident drill.

### Developer profile and weak areas
- 3+ years of Spring Boot. Has used Redis, Keycloak and Vault, but only as already set up by DevOps, without deep understanding.
- Has seen pod restarts at work, but someone else found the root cause (leak, heap growth, busy threads).
- Self-assessed weak in all of: concurrency, transactions, k8s, observability, security, system design. Has never used Testcontainers.
- Chose to defer the architecture decisions (balance model, PII encryption, Kafka rationale, SLO). They come back as
  required inputs to ticket designs (see "Deferred" in `docs/decisions/sprint-0-decisions.md`).
- Took the tech lead's recommendation on currency (USD only) without debate. That's fine here, but watch for
  a pattern of accepting defaults without reasoning.
- First design (WAL-0.1, 2026-11-24) was an implementation log, not a design: no decisions, edge cases and risks answered with
  "no comment". The design was written after the code. Round 2: made a good team argument for Groovy (pushback accepted), but answered concurrent Flyway wrongly and asked to skip
  design review to save time. 2026-11-24: chose to drop the design step for good (D30). Decisions now go in the PR description; watch whether
  they are real answers or "no comment" again. Under time pressure they cut thinking first.
- PR #1: asked for review on code that ignored a known review checklist, and the PR description was empty (wrong ticket ref). Coach:
  self-review against the checklist before asking; use draft PRs with task boxes.
- Round 4: fixes the checklist items literally (file created, item ticked) without verifying the outcome (container never started;
  T5 evidence misread). Pattern: "done" = "changed", not "proved". Coach: for each fix, say how you verified it.
  Decisions section: describes ("keep the lock enabled", "heap within limits") instead of deciding (numbers, consequences).

### Known risks
- WSL2 has no `.wslconfig`, so it gets about 8 GB. The full stack may get OOM-killed (WAL-0 caps it at 6 GB).

### Open review comments
- WAL-0.1 PR #1, round 4 (2026-11-24): request changes. 5 blockers (shared container config not imported, `profiles.active: local` in main
  config, `application-local.yml` with creds committed, T5 evidence wrong, nothing pushed), majors (unused `thoupay` schema / F6 undecided,
  weak count(*) assertion, `Refs: WAL-1`, vague Decisions, AC 2 and AC 7 proofs missing), minors (AC 3/4 tests, ADR-000, runbook, commits).
  Details in `review.md`.

### Completed incidents
- None yet.

### Skill assessments
- None yet (the first one comes after about 3 tickets).
