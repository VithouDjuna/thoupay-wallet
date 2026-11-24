# Discussion: ThouPay Wallet

From: Tech Lead / PO / SRE (Claude)
Last updated: 2026-11-24
Status: **WAL-0.1 PR #1: code review round 4 → request changes.**

## How this file works
- This file always shows the **current state** of our discussion. After each conversation I rewrite it.
- Sprint 0 decisions are closed and live in `docs/decisions/sprint-0-decisions.md`.

---

## 💬 Code review of PR #1, round 4 (2026-11-24)

Full review: `docs/backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/review.md`.

**You didn't push.** PR #1 still points at `dece471`. I reviewed your local working tree instead.

**Good progress:** 6 of the 10 round-3 items are fixed (V2–V4 gone, clean V1, module packages, old test deleted, `clean-disabled`, rename).

**But the main blocker is still there.** I ran `./gradlew test` on your machine. The log says
`Database: jdbc:postgresql://localhost:5432/postgres`. No container started. Two reasons:
1. `PostgresTestConfiguration` is never imported. Component scan skips `@TestConfiguration` classes.
2. `spring.profiles.active: local` in the main `application.yml` points every environment at your dev DB.

Your "T5" error is the evidence: `ConnectException` means "port 5432 refused", not "Docker is down".

**A senior habit to build now:** after each fix, say *how you verified it*. "I added the config class" is a change.
"The log shows a Testcontainers container on a random port" is proof.

---

## Still open on your side
- Set `JAVA_HOME` to JDK 21 before the first drill.

## Next step
Fix the blockers, then: `docker stop thoupaydb` → `./gradlew build` must be **green**. Stop Docker Desktop → must be **red** with a clear message.
Push to PR #1, update the PR body, say "ready". Target: Thu 2026-11-26.
