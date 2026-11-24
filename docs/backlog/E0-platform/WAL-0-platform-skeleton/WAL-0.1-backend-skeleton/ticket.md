# WAL-0.1: Backend skeleton + module boundaries

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.1-backend-skeleton` |
| Depends on | – |
| Estimate | 6 h (was 4 h; +2 h for learning Flyway and Testcontainers) |
| Design due | **Wed 2026-11-25** |
| Target done | Thu 2026-11-26 |

> 📖 **New to some of this?** Read [the friendly walkthrough of this ticket](../../../../learning/wal-0.1-backend-skeleton-explained.md) first.

## Goal
An empty Spring Boot application with the right structure. It builds, starts, and passes one real integration test.

## Why this ticket exists
Every later ticket builds on this skeleton. If the foundations are wrong, every later ticket pays for it:
- **Module boundaries:** without enforcement, `transfer` will end up reading `ledger` tables directly within a month.
- **Flyway:** the database holds money. Schema changes must be versioned, reviewed and repeatable on a laptop, in CI and in k8s.
  Hibernate `ddl-auto=update` can't do that: it never drops or renames, keeps no history and can't run zero-downtime (expand/contract) migrations.
- **Testcontainers:** the ledger and transfer tickets will test locking, isolation and constraints. Those only behave correctly on
  real PostgreSQL. H2 or mocks would let wrong code pass.

## Scope
- `backend/`: **Gradle Kotlin DSL** (`build.gradle.kts`, `settings.gradle.kts`), Java 21 toolchain, Spring Boot 4 (latest 4.x),
  Gradle wrapper committed.
- A modular monolith with the modules `identity`, `wallet`, `ledger`, `transfer`, `risk`, `admin` and `notification`.
  The modules are empty, but a test or the build **enforces** their boundaries. Folder names alone don't count.
- Flyway is wired in and runs on startup. Decide on the schema layout: one schema, or one per module. Details below.
- Actuator (`health`, `info`) and springdoc (`/v3/api-docs`, Swagger UI) are on.
- RFC 9457 `problem+json` is the default error format. For example, an unknown URL returns a 404 `problem+json`.
- One Testcontainers integration test starts PostgreSQL and the app context, and checks that Flyway ran. Details below.

### Flyway requirements
| # | Requirement | Why |
|---|---|---|
| F1 | Migrations live in `src/main/resources/db/migration` and use the name format `V<version>__<description>.sql` (two underscores), e.g. `V1__create_customer_table.sql`. | This is Flyway's default location and naming. Anything else needs extra config, and every reader has to learn it. |
| F2 | There is at least one versioned migration, `V1`. It creates the schema layout you choose in the design, and **no business tables**. | Tables belong to WAL-1 and later tickets. V1 only proves the pipeline works. |
| F3 | Hibernate never changes the schema. Set `spring.jpa.hibernate.ddl-auto` to `validate` (or `none`, if you can justify it). | Flyway is the only thing allowed to change the schema. |
| F4 | `flyway clean` (which drops everything) must not be possible outside tests. Set it explicitly in config, even where the default is already safe. | One wrong property in prod wipes every wallet. Being explicit documents the intent. |
| F5 | Team rule, written in the runbook: **never edit a migration that has already run anywhere.** Add a new version instead. | Flyway stores a checksum for each applied file. If you edit an applied file, the next startup fails validation. |
| F6 | The design says where `flyway_schema_history` lives, given your schema layout. | With several schemas, the history table goes in only one of them. Decide which one on purpose. |

### Testcontainers requirements
| # | Requirement | Why |
|---|---|---|
| T1 | Use **PostgreSQL 18**, with the image tag pinned to an exact version (e.g. `postgres:18.1-alpine`). Never use `latest`. WAL-0.3 (compose) and the k8s manifests use the same tag. | Tests must run against the same database you run in production. With `latest`, a build that passed yesterday can fail today. |
| T2 | Spring connects to the container without hard-coded URLs or ports. Use Spring Boot's service connection support. Compare it with `@DynamicPropertySource` in the design. | Testcontainers maps the container to a random host port, so a fixed URL won't work. |
| T3 | **One container for the whole test run**, shared by every integration test class. It is not restarted for each class. | You'll have dozens of integration tests. Restarting the container for each class costs 3–5 s per class. |
| T4 | The test asserts **in the database** that `V1` was applied successfully. Checking that "the context started" or reading a log line doesn't count. | A context can start even when Flyway is misconfigured, for example when it scans the wrong location and finds zero migrations. |
| T5 | If Docker is not available, the test **fails** with a clear message. It must not be skipped silently. | A skipped test turns CI green while nothing was tested. |
| T6 | The test is part of `./gradlew build`. It needs nothing installed except a running Docker. | A new team member can clone the repo and build it. That's also what CI does in WAL-0.2. |

### Running the app locally (no compose yet)
WAL-0.3 adds docker-compose. Until then, choose one option and write it in the runbook:
- **A (recommended):** Spring Boot's development-time Testcontainers support. A test-scope main class starts the app with the
  same container setup as the tests, and you run it with `./gradlew bootTestRun`. Search for "Testcontainers at development time" in the Spring Boot reference.
- **B:** a local PostgreSQL 18 install with the credentials in an ignored local profile. Never commit real passwords.

## Out of scope
Security, Vault, the logging pipeline and metrics come in later sub-tickets. Use a plain local datasource for now.

## Acceptance criteria
1. `./gradlew build` passes on your machine, including the Testcontainers test.
2. A test fails if, for example, `wallet` uses an internal class of `ledger`. Show me that it fails.
3. `GET /does-not-exist` returns `404` with `Content-Type: application/problem+json`.
4. `/v3/api-docs` returns a valid OpenAPI document.
5. The app starts in under 20 s on your laptop.
6. Flyway requirements F1–F6 and Testcontainers requirements T1–T6 are met.
7. Proof that F5 works: you edit an applied migration locally, Flyway refuses to start, and you paste the error into the PR description. Then revert the edit.
   This needs a database that **keeps its data between runs** (for example, a local Postgres, or a plain `docker run postgres:18.1-alpine` with a fixed port).
   A Testcontainers database starts empty every time, so it never shows this error. Explain in the PR why that is.

## Edge cases (answer them in the design)
- Packages or Gradle subprojects: what does each one cost, and how is each one enforced?
- How much heap does the app get? Set it explicitly (no JVM defaults), and write down the number and the reason.
- **One schema or one per module?** If you choose one schema, how do you stop `wallet` SQL from joining `ledger` tables?
  If you choose one per module, how does each module's migration end up in the right schema, and who creates the schemas?
- **Two pods start at the same time** and both run Flyway. What happens? Is it safe? (Find out how Flyway handles concurrent runs.)
- **Should migrations run at app startup at all,** or in a separate k8s Job or init container? You don't need to decide yet.
  Write down the trade-off under "Risks".
- **Docker isn't running** when someone runs `./gradlew build`. What does that person see?

---

## Primer: what you need to know first
You haven't used these tools before, so read this before the design. It's background, not a solution.

### Flyway in 6 points
> New to Flyway? Start with [docs/learning/flyway-basics.md](../../../../learning/flyway-basics.md): what a migration script is, plus a 10-minute exercise.

1. On startup, Flyway compares the files in `db/migration` with the `flyway_schema_history` table and applies any versions that are missing, in order.
2. Every applied migration gets a row in that table: version, description, checksum, `success`, execution time and who ran it.
3. **Versioned** migrations (`V…`) run once. **Repeatable** migrations (`R__…`) run again whenever their checksum changes,
   which suits views and functions. You only need versioned ones now.
4. Each migration runs in a transaction. PostgreSQL supports transactional DDL, so a failed migration rolls back completely.
   Exception: `CREATE INDEX CONCURRENTLY` can't run in a transaction. You'll meet that in a later zero-downtime ticket.
5. In Spring Boot 4, Flyway needs **both** the Boot Flyway starter and the `flyway-database-postgresql` module. Without the
   database module, Flyway fails with an "unsupported database" error.
6. The `spring.flyway.*` properties you'll most likely use: `locations`, `schemas`, `create-schemas`, `default-schema`,
   `clean-disabled`, `validate-on-migrate`, `baseline-on-migrate`. Find out what each one does, and use only the ones you need.

### Testcontainers in 6 points
> New to Testcontainers? Start with [docs/learning/testcontainers-basics.md](../../../../learning/testcontainers-basics.md): what it is, why the port is random, container lifecycle, plus a 15-minute exercise.

1. Testcontainers is a library that starts real Docker containers from a JUnit test and removes them afterwards.
   A helper container called **Ryuk** cleans up even if the JVM crashes. You'll see it in `docker ps`.
2. On Windows, it needs **Docker Desktop with the WSL2 backend running**. Check that first: `docker run --rm hello-world`.
3. The first run pulls the image, which takes a minute or more. Later runs start Postgres in about 2–4 s.
4. **Watch the version.** This project uses Testcontainers 2.x, where artifact names and some packages changed.
   Many blog posts and Stack Overflow answers show 1.x code that won't compile. Check which version a post is for.
5. Spring Boot gives you two ways to connect to the container: `@ServiceConnection` (Boot creates the connection details)
   or `@DynamicPropertySource` (you set `spring.datasource.*` yourself). Know both, choose one, and say why in the design.
6. How you share one container across test classes (T3): look at Spring's test context caching and the "singleton container" pattern.
   Also look up why `@Testcontainers` with a `static` `@Container` field on each test class doesn't give you that by itself.

### Pitfalls people hit in week 1
- `BackendApplicationTests` (the generated `contextLoads()` test) now also needs a database. Either use the same container
  setup or delete it, because it duplicates your integration test. Don't leave it failing or `@Disabled`.
- Using `@Testcontainers(disabledWithoutDocker = true)` breaks T5.
- With per-module schemas, forgetting that the history table and the default schema are separate settings.
- Editing `V1` while you experiment, then wondering why startup fails. On your own laptop you can recover by recreating the container.
  Anywhere shared, the only answer is a new version.
- WSL2 memory: Docker Desktop plus Gradle plus your IDE can use most of the WSL2 memory limit (see Known risks in `PROJECT.md`).

### References (official docs first)
- Spring Boot, database migrations with Flyway: https://docs.spring.io/spring-boot/how-to/data-initialization.html
- Spring Boot, Testcontainers support (service connections, development-time use): https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Flyway documentation: https://documentation.red-gate.com/flyway
- Testcontainers for Java: https://java.testcontainers.org/
- Spring Modulith, verifying module structure: https://docs.spring.io/spring-modulith/reference/verification.html
- RFC 9457 (Problem Details): https://www.rfc-editor.org/rfc/rfc9457

---

## Suggested order of work
1. **Learning spike, about 1 h.** Use a throwaway branch `spike/flyway-testcontainers` and delete it afterwards. The code you already have in `backend/` counts as this spike.
   - Start Postgres with Testcontainers, add a `V1`, run the test, and look at `flyway_schema_history`. You can connect to the
     container with `docker exec -it <id> psql -U test` while the test is paused at a breakpoint.
   - Edit `V1` after it ran, run again, and read the error. Add a `V2` and see how it's applied.
   - Break the SQL in a migration on purpose, and check that the build goes red.
2. **Write `design.md`** (due Wed). Answer every edge case above. Write down what the spike taught you.
3. **Design review with me.** No feature-branch code before this.
4. After the review, on `feature/WAL-0.1-backend-skeleton`:
   1. Convert the build to Kotlin DSL and check that it still builds.
   2. Create the module packages and the boundary test, then break a boundary on purpose (AC 2).
   3. Add Flyway with `V1`, then the shared Testcontainers setup, then the DB assertion (T4).
   4. Add the `problem+json` 404, springdoc and Actuator.
   5. Set the heap size and measure startup time (AC 5).
   6. Write ADR-000 and the runbook section. Open the PR.

## Definition of Done
- [ ] Design reviewed and approved before implementation
- [ ] Acceptance criteria 1–7 pass
- [ ] ADR-000 in `docs/decisions/`: module boundaries and how they are enforced
- [ ] Runbook section: how to build and run the backend locally, how to add a migration, and what to do when Flyway validation fails
