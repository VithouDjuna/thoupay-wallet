# Sprint 0 Discussion: ThouPay Wallet

From: Tech Lead / PO / SRE (Claude)
Last updated: 2026-11-24
Status: **closed ✅.** Every decision is in `docs/decisions/sprint-0-decisions.md`. No open questions.

## How this file works
- This file always shows the **current state** of our discussion. After each conversation I rewrite it.
- When a new topic needs discussion (a design debate, a change request), it opens here again.

---

## 💬 My response to your last question (2026-11-24): "why Flyway, why Testcontainers in WAL-0.1?"

**Answered, and the ticket is expanded.** `WAL-0.1-backend-skeleton/ticket.md` now has:
- "Why this ticket exists": the reasons for the module boundaries, Flyway and Testcontainers.
- Concrete requirements **F1–F6** (Flyway) and **T1–T6** (Testcontainers), and new ACs 6–7.
- New design edge cases: schema layout, two pods running Flyway at once, migrations at startup vs a Job, Docker not running.
- A primer, the week-1 pitfalls, official doc links and a suggested order of work (spike → design → review → build).
- Estimate raised from 4 h to 6 h for learning time.

**Fixed on my side:** `docs/architecture/tech-stack.md` said Spring Boot 3, but the ticket says Boot 4. It now says
Boot 4 + Gradle Kotlin DSL, and pins **PostgreSQL 18**.

**Tech lead notes on the code already in `backend/`:**
1. It's a spike until the design is reviewed. Put what you learned into `design.md`.
2. `build.gradle` is Groovy, but the ticket requires the Kotlin DSL. Convert it, or push back in the design with a reason.

---

## Still open on your side (housekeeping, none of it blocks the design)
1. **First push:** the GitHub remote is still empty. Before you push, rename the branch, since D4 says trunk = `main`:
   `git branch -m master main` and then `git push -u origin main`.
2. **Old parent repo:** `C:\Users\thenc\Project` still has `origin` = `thoupay-wallet.git`. Fix it:
   `git -C C:\Users\thenc\Project remote set-url origin https://github.com/VithouDjuna/easy-common.git`
3. **Old folder:** `C:\Users\thenc\Project\project-stepup` still exists. Delete it when you're ready. I won't.
4. **Before the first drill:** set `JAVA_HOME` to JDK 21.

---

## Next step
**WAL-0: Platform skeleton** is in `docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`.
It's split into 7 sub-tickets (see the table in `docs/backlog/E0-platform/WAL-0-platform-skeleton/ticket.md`). Start with **WAL-0.1**: write
`docs/backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/design.md` by **Wed 2026-11-25**. No code before I've reviewed it.
