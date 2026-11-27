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
| [docs/learning/](docs/learning/) | Learning notes: `wal-0.1-backend-skeleton-explained.md` (ticket walkthrough), `flyway-basics.md`, `testcontainers-basics.md`, `ci-basics.md` (WAL-0.2) |
| [docs/runbooks/](docs/runbooks/), [docs/postmortems/](docs/postmortems/) | Operations notes, incident write-ups |
| [discussion.md](discussion.md) | Current open discussion with the tech lead |

---

## Project status (team memory, updated every session)
> Claude reads this at the start of every session. Chat history does not carry over between sessions,
> so this file and `discussion.md` are the only memory that does.

**Last updated:** 2026-11-26 (WAL-0.1 PR #2 approved)

### Current sprint and ticket
- **Sprint 0** discussion is closed. All decisions are in `docs/decisions/sprint-0-decisions.md`.
- **Current ticket:** WAL-0, Platform skeleton (`docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`), given 2026-11-24.
  - Split into 7 sub-tickets (WAL-0.1 … WAL-0.7), each with its own design, review and PR (design step back since D31, 2026-11-26). Story target: Sun 2026-12-06.
  - **Now:** WAL-0.2 CI pipeline (given 2026-11-26). **Design ✅ approved 2026-11-26** (round 1, done by chat discussion: 6 questions one at a time, Claude wrote the answers into `design.md`). Next: developer implements on `feature/WAL-0.2-ci-pipeline`. Lesson: written design templates overwhelmed the developer (frustrated after the first Q1 attempt); one small question at a time, explaining each concept **before** asking, worked. They needed secrets and the AC 1/AC 2 conflict explained from zero before they could decide. WAL-0.1 merged as `683dea5` after 8 rounds, but merged with the Testcontainers blocker unfixed (tests silently used the local DB via `SPRING_DATASOURCE_*` env vars; main red without them). Fix landed as PR #2 (`@FullContextTest` meta-annotation imports `PostgresTestConfiguration`); approved 2026-11-26 round 9, verified green with no env vars on a Testcontainers DB. Follow-ups carried: heap + Flyway-lock Decisions and schema layout → WAL-1; runbook, ADR-000, AC 7, T5 → WAL-0.7. New merge gate: green build + ACs + short Decisions; everything else = follow-ups.
  - Round 5 (head a58315e): container blocker fixed and verified (`@IntegrationTest` meta-annotation imports the config; Flyway hits the
    Testcontainers DB). But the build is red, 2/2 tests failing (verified by running it): no V1 migration exists (never committed, deleted
    locally after the round-4 "V1 may not need CREATE SCHEMA" hint), and the AC 2 boundary violation was committed instead of reverted.
    `profiles.active: local` still in main config; `.gitignore` uses `!application-local.yml` (un-ignores it).
  - Learning notes written for the developer (who is new to Flyway and Testcontainers): `docs/learning/`. They learn best from
    friendly, analogy-based, step-by-step explanations with hands-on exercises.
  - Status: waiting for fixes pushed to PR #1. Sub-ticket status table: `docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`.
- **Next:** WAL-1 (wallet creation + balance), then WAL-2 (ledger). Milestone 1 = Phases 0–2 by 2026-12-31.

### Deliberately deferred by the developer (2026-11-26)
- WAL-0.1 open items (heap / `-Xmx` sizing with numbers, Flyway lock behaviour, schema layout, runbook, ADR-000, AC 7, T5) are left open **on purpose**.
  The developer wants to understand them by meeting the real problem first, not by filling in a Decision up front.
- Plan: teach them through **incident drills**, not review comments. First candidate: "pods OOMKilled (exit 137), JVM heap vs container limit"
  once the app runs in k8s (after WAL-0.3). After each drill, the developer writes the real Decision (with numbers) from what they saw.
- Do not re-raise these as review blockers. Schema layout is the exception: it must be chosen in WAL-1, because WAL-1 creates the first tables.

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
- Round 5: asked for review on a red build (never ran `./gradlew build` before "ready"). Third round in a row of "changed, not proved".
  Misread a hint as an instruction to delete V1. Decisions section unchanged between rounds. Coach: build green locally is the entry
  ticket for a review; read review hints as questions to verify, not commands.

- 2026-11-26 retro written: `docs/feedback/WAL-0.1-retro.md` (top 3: changed ≠ proved, cutting thinking under pressure,
  reading hints as commands). The developer then asked to bring the design step back (D31): `design.md` templates created for WAL-0.2…0.7. WAL-0.2 goal: approved in ≤ 2 rounds, PR body uses the self-check block. Check this in the WAL-0.2 review.

### Known risks
- WSL2 has no `.wslconfig`, so it gets about 8 GB. The full stack may get OOM-killed (WAL-0 caps it at 6 GB).

### Open review comments
- WAL-0.1 PR #2, round 9 (2026-11-26): ✅ approved, no blockers. Before merge: fill the empty PR body (Refs + 1 Decision + pasted proof)
  and squash as `fix(WAL-0.1): …`. Follow-ups for WAL-0.2: CI must run without `SPRING_DATASOURCE_*` (guard against regression);
  use the next ticket's branch for fixes. Details in `review.md`.
- Pattern still open: PR body empty again, with no proof pasted (the "changed, not proved" habit). The code itself was right first try this round.

### Completed incidents
- None yet.

### Skill assessments
- None yet (the first one comes after about 3 tickets).
