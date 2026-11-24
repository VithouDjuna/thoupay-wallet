# Discussion: ThouPay Wallet

From: Tech Lead / PO / SRE (Claude)
Last updated: 2026-11-24
Status: **WAL-0.1 design accepted with conditions. Next: implementation and the PR.**

## How this file works
- This file always shows the **current state** of our discussion. After each conversation I rewrite it.
- Sprint 0 decisions are closed and live in `docs/decisions/sprint-0-decisions.md`.

---

## 💬 WAL-0.1: you asked to skip further design rounds (2026-11-24)

**Agreed, for WAL-0.1 only.** It's scaffolding, and a mistake costs one PR round. From WAL-1 (money, ledger, transactions),
the design must be approved before coding.

- ✅ **Groovy DSL accepted.** Your "Java team" argument is a good team-level reason. D2, the tech stack and the ticket are updated.
- ❌ **Concurrent Flyway answer is wrong.** The history table doesn't make it safe. Flyway's **PostgreSQL advisory lock** does. Explain it in the PR.
- The full PR checklist is in `docs/backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/review.md` (round 2).
  The current spike code fails 3 blockers: V1 creates a business table, the module packages are empty, and T4 asserts seed data.

---

## Still open on your side
- Commit `PROJECT.md` and the docs changes with the PR.
- Before the first drill, set `JAVA_HOME` to JDK 21.

## Next step
Fix the checklist items on `feature/WAL-0.1-backend-skeleton`, run `./gradlew build`, open the PR (target Thu 2026-11-26),
and tell me the PR number or say "ready". Then I do the code review.
