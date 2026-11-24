# Review: WAL-0.1 (PR #1), round 4 (2026-11-24)

**Verdict: ❌ Request changes.** Nothing was pushed: PR #1 still points at `dece471`. This review covers your **local working tree**.
Real progress since round 3: 6 of the 10 round-3 items are fixed. But the most important blocker is still there, just moved.

**Proof (I ran `./gradlew test` on your machine with `thoupaydb` running):**
```
FlywayExecutor : Database: jdbc:postgresql://localhost:5432/postgres (PostgreSQL 18.1)
BUILD SUCCESSFUL
```
No Testcontainers container started. Your integration test migrated your **dev database**.

## Must fix (blockers)
- [ ] `ApplicationIntegrationTest.java:14`: `PostgresTestConfiguration` is never used. `@TestConfiguration` classes are **not** picked up by component scan. → `@Import(PostgresTestConfiguration.class)` (or one shared base annotation/class every IT uses).
- [ ] `application.yml:2-4`: `spring.profiles.active: local` in the main config makes **every** environment (tests, CI, k8s) use `localhost:5432` with `admin/admin`. → Remove it. Activate `local` only from the outside (`SPRING_PROFILES_ACTIVE`, IDE run config) or use option A (`bootTestRun`).
- [ ] `application-local.yml`: committed and not git-ignored, so the round-3 credentials finding is not fixed, only renamed. → Either git-ignore it and commit an `application-local.yml.example`, or go with option A and delete it.
- [ ] PR body, T5: that stack trace is `PSQLException → ConnectException`, i.e. "port 5432 refused". It's what you get when `thoupaydb` is stopped, **not** when Docker is stopped. It proves the blocker above. → After the fix, stop Docker Desktop and paste the Testcontainers error (`Could not find a valid Docker environment`).
- [ ] PR #1: push the commits. A review request on an unchanged PR wastes the reviewer's time.

## Should fix (majors)
- [ ] `V1__init_schema.sql`: creates `thoupay`, but nothing uses it. Flyway history, Hibernate and future tables all go to `public`. → Decide: one schema (set `spring.flyway.default-schema` + `hibernate.default_schema`) or one per module. Write it in Decisions (F6: where does `flyway_schema_history` live?). Hint: with `spring.flyway.schemas` set, Flyway can create the schema itself, so V1 may not need `CREATE SCHEMA`.
- [ ] `ApplicationIntegrationTest.java:30-32`: `count(*) = 1` breaks as soon as V2 lands, and doesn't check `success`. → Assert `version = '1'` and `success = true` (AssertJ).
- [ ] PR body: still `Refs: WAL-1`. → `Refs: WAL-0.1`.
- [ ] PR Decisions are descriptions, not decisions:
  - Advisory lock: "keep it enabled" → say **what happens** to pod B (waits, then sees V1 applied, does nothing).
  - Heap: "keep it within limits" → give **the number** (512m) **and why** (e.g. leaves ~X GB of the 8 GB WSL2 for Docker + IDE).
  - Missing: one schema vs per module, migrate at startup vs k8s Job (trade-off under Risks), packages vs Gradle subprojects.
- [ ] AC 2: no proof that a boundary violation fails `ModularityTest`. → Add a `wallet → ledger.internal` call, paste the failure, revert.
- [ ] AC 7: no Flyway checksum error in the PR. Your `thoupaydb` keeps data, so you can do it now.

## Also missing (minors)
- [ ] AC 3/4 as **tests**, not curls. And AC 3 is `GET /does-not-exist`, not `/v3/api-docs/not-found`.
- [ ] `build.gradle:21-26`: heap set only for `test` and `bootRun`. Fine for now; note in the runbook that the container sets it in WAL-0.3+.
- [ ] ADR-000 (module boundaries) and the runbook section.
- [ ] Commits: separate docs (`PROJECT.md`, `discussion.md`, backlog) from code.

## Nits
- `ApplicationIntegrationTest.java:5,32`: still JUnit `Assertions`. → AssertJ only.
- `ApplicationIntegrationTest.java:15`, `ModularityTest.java:6`: JUnit 5 test classes don't need `public`.
- `package-info.java`, `application-local.yml`: no newline at end of file.

## 👍 Fixed since round 3
V2–V4 deleted, V1 has no business table, `package-info.java` in all 7 modules, `BackendApplicationTests` deleted,
`clean-disabled: true`, test renamed, startup time measured (4.9 s), a Decisions section exists, and a real risk (shared test data).

## Next
1. Fix the 5 blockers. 2. `docker stop thoupaydb`, then `./gradlew build`: **must still be green**. 3. Stop Docker Desktop, run again: must be red with a clear message.
4. Push, update the PR body, say "ready".
