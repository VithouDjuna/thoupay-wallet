# WAL-0.1 Backend skeleton, explained by your lead 🍻

> The ticket: [WAL-0.1 ticket.md](../backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/ticket.md).
> Read this first, then the ticket will make sense line by line.
> For the two new tools, see [flyway-basics.md](flyway-basics.md) and [testcontainers-basics.md](testcontainers-basics.md).

---

## Part 0: what is this ticket, really?

Picture building a house 🏠.
Before anyone picks paint colours or furniture (wallets, transfers, payments), someone has to pour the **foundation**,
put up the **walls between the rooms**, and connect **water and electricity**.

**WAL-0.1 is the foundation.** At the end there's *no business feature at all*. No wallet, no money. Just an empty app that:
- builds ✅
- starts ✅
- has walls between its rooms that nobody can break through ✅
- talks to a real database, with versioned changes ✅
- speaks a clean, standard language for errors and API docs ✅

**"Why spend a whole ticket on an empty app?!"** Because every later ticket stands on this one.
Fixing a cracked foundation later means lifting the whole house. Fixing it now is cheap. That's a very "senior" way to think:
**the boring stuff first, done right.**

Let's walk through the ticket, piece by piece.

---

## Part 1: the build setup (Gradle Kotlin DSL, Java 21, Spring Boot 4)

**What the ticket says:** `build.gradle.kts` (Kotlin DSL), Java 21 toolchain, Spring Boot 4, Gradle wrapper committed.

**In friend terms:**
- **Gradle** = the kitchen that turns your code into a runnable app. You already know this one.
- **Kotlin DSL** = writing the Gradle file in Kotlin (`build.gradle.kts`) instead of Groovy (`build.gradle`).
  Same kitchen, different language for the recipe. Kotlin is *typed*, so IntelliJ can autocomplete it and
  shows a red line when you make a typo, instead of the build failing mysteriously later. It's also Gradle's own default for new projects now.
- **Java 21 toolchain** = "build with Java 21, whatever Java happens to be installed on this machine." Gradle fetches the right JDK itself.
  That way "it works on my Java 17 laptop" never happens.
- **Gradle wrapper** (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`) = a tiny script that downloads the *exact* Gradle version.
  A teammate or CI just runs `./gradlew build` with no "which Gradle do you have?" questions.

**🚩 Heads-up:** your current spike uses `build.gradle` (Groovy). The ticket says Kotlin DSL.
Either convert it, or argue in your design why you shouldn't. **Never just quietly change a requirement.** That's a trust thing with your lead. 🙂

---

## Part 2: the modular monolith (the most important part of this ticket) 🧱

### First, the two classic ways to build a backend

```
😵 Big ball of mud (monolith with no rules)      🤯 Microservices
┌───────────────────────────┐                     ┌──────┐  ┌──────┐  ┌──────┐
│ everything calls          │                     │wallet│  │ledger│  │transf│
│ everything, any class can │                     └──┬───┘  └──┬───┘  └──┬───┘
│ touch any table           │                        └── network calls ──┘
└───────────────────────────┘                     + Kafka, retries, distributed transactions...
  easy at first, a nightmare at month 6           clean walls, but VERY expensive for a small team
```

### The middle path: the modular monolith
**One app, one deployment, but strict walls between the rooms.**

```
┌──────────────────────── one Spring Boot app ────────────────────────┐
│  ┌─────────┐   ┌────────┐   ┌────────┐   ┌──────────┐               │
│  │identity │   │ wallet │──▶│ ledger │◀──│ transfer │   ...         │
│  └─────────┘   └────────┘   └────────┘   └──────────┘               │
│                     only through the "front door" (public API)      │
└─────────────────────────────────────────────────────────────────────┘
```

The ticket's 7 rooms: `identity`, `wallet`, `ledger`, `transfer`, `risk`, `admin`, `notification`.

### The "front door" idea 🚪
Each module has:
- a **front door**: a few public classes other modules are *allowed* to use (e.g. a `LedgerService` interface)
- **private rooms**: everything else (repositories, entities, helpers). Other modules must never touch these.

**Why this matters for money:** imagine `transfer` reads the `ledger` tables directly to "quickly check a balance".
Then one day `ledger` changes how it stores entries, and transfers silently compute the wrong balances. 💸
With walls, `ledger` can change its insides freely, as long as its front door stays the same.

And bonus: if ThouPay grows huge one day, a clean module can be pulled out into its own microservice *much* more easily.

### "Folder names alone don't count". What does the ticket mean?
Creating folders `wallet/`, `ledger/` is easy. But **folders don't stop anybody**. A tired developer on a Friday will
happily write `import ...ledger.internal.LedgerEntryRepository` inside `wallet`, and Java compiles it without a word.

So the ticket wants a **guard** 💂: something that makes the **build fail** when a wall is broken.
That's acceptance criterion 2: *"show me that it fails"*. You break a wall on purpose, the build goes red, and you screenshot it.

### Your two options for the guard (edge case 1 in the ticket)

| | Option A: **packages** + a test that checks them | Option B: **Gradle subprojects** (one per module) |
|---|---|---|
| Looks like | one project, `io.github...backend.wallet`, `...ledger`, ... | `backend/wallet/build.gradle.kts`, `backend/ledger/build.gradle.kts`, ... |
| Who stops a bad import | a **test** fails (e.g. Spring Modulith's verify) | the **compiler** refuses (the module isn't even on the classpath) |
| When you find out | when the tests run | the moment you type it in the IDE |
| Cost | cheap and simple, one build file | 7+ build files, more Gradle knowledge, slower to set up |
| Moving code between modules | easy | more friction |

Your spike already has a `ModularityTest` using **Spring Modulith** (`ApplicationModules.of(...).verify()`). That's option A. 👍
But don't just keep it because it's already there. **Explain the trade-off in your design**, as if I'd pushed you toward option B.

**Things to look up (Spring Modulith):**
- How does Modulith decide *what a module is*? (Hint: look at the packages directly under your main application class's package.)
- What counts as the "front door" by default, and what counts as "internal"? (Hint: sub-packages.)
- Can a module open extra doors on purpose? (Look up "named interfaces".)
- Does `verify()` also catch **cycles** (`wallet → ledger → wallet`)? Why are cycles bad?

**And one gotcha:** with *empty* modules (no classes inside), does Modulith even see them? 🤔 Test that. It's part of proving AC 2.

### Where the database fits in
Walls in Java code are only half the story. If all tables sit in one schema, `wallet` SQL can still `JOIN` `ledger` tables,
and no Java test will notice. That's why the ticket asks you **"one schema or one per module?"** It's the *database side* of the same walls.
(Schemas are explained in [flyway-basics.md](flyway-basics.md), Part 7.)

### ADR-000: writing the decision down 📝
**ADR = Architecture Decision Record.** It's a short markdown file that says:
- **Context:** what problem we had
- **Decision:** what we chose
- **Alternatives:** what we didn't choose, and why
- **Consequences:** what this costs us and what it makes easier

**Why bother?** In 6 months a new teammate will ask *"why on earth are these packages and not subprojects?"*.
The ADR answers without you being in the room. Seniors write these. Juniors leave the answer in their heads. 😉
Keep it to one page. Put it in `docs/decisions/ADR-000-module-boundaries.md`.

---

## Part 3: Flyway 🗂️

Fully explained in [flyway-basics.md](flyway-basics.md). In one sentence:
**versioned SQL files that build the database the same way everywhere, applied automatically when the app starts.**

What the ticket wants from you here, in friend terms:
- **F1:** files in `db/migration`, named `V1__something.sql` (two underscores!)
- **F2:** your V1 only creates the schema layout (the "rooms"), with no tables yet
- **F3:** Hibernate is only the inspector (`validate`), never the builder
- **F4:** the "delete everything" button (`flyway clean`) is switched off
- **F5:** never edit an old migration
- **F6:** decide where the stamp card (`flyway_schema_history`) lives

---

## Part 4: Testcontainers 🐳

Fully explained in [testcontainers-basics.md](testcontainers-basics.md). In one sentence:
**every test run gets a brand-new real Postgres in Docker, which is thrown away afterwards.**

What the ticket wants, in friend terms:
- **T1:** Postgres 18 with an exact pinned version (never `latest`)
- **T2:** no hard-coded URL, because the port is random
- **T3:** one container for the whole test run, so tests stay fast
- **T4:** prove *in the database* that Flyway ran (look at the stamp card!)
- **T5:** no Docker → the test **fails loudly**, never skips silently
- **T6:** it runs as part of `./gradlew build`

---

## Part 5: Actuator (the app's health check) 🩺

**What it is:** Spring Boot endpoints that tell the outside world about your app.
- `/actuator/health` → `{"status":"UP"}`, which means "I'm alive and OK"
- `/actuator/info` → "who am I": app name, version, build time, ...

**Why you need it now:** later, **Kubernetes** will call `/actuator/health` every few seconds, like a nurse checking your pulse 💓.
If it says DOWN, k8s restarts or stops sending traffic to that pod. Our incident drills will play *a lot* with this.
(WAL-0.4 builds on it, so read that ticket carefully when you get there. 😉)

**What the ticket wants:** only `health` and `info` are exposed over HTTP.
**Why not everything?** Endpoints like `env`, `heapdump` and `configprops` leak secrets and memory contents. Exposing them publicly is a classic security hole.
Your current `application.yml` already only exposes those two. 👍

**Try it:** start the app → `curl localhost:8080/actuator/health`. Then try `/actuator/env`. You should get a 404.

---

## Part 6: springdoc / OpenAPI (your API's "menu") 📜

**What it is:** springdoc reads your controllers and auto-generates a **description of your whole API**:
every URL, request body, response and status code. In two flavours:
- `/v3/api-docs` → a JSON file for machines
- `/swagger-ui.html` → a web page for humans, where you can click "Try it out"

**Why it matters (especially to me, your frontend dev 😄):** I'll **generate my TypeScript types from your `/v3/api-docs`**.
If the document is wrong, my frontend is wrong. If you change a field without telling me, my build breaks, and that's *good*, because we catch it early.
So that JSON is a **contract** between us, not decoration.

**What the ticket wants:** AC 4 says `/v3/api-docs` returns a *valid* OpenAPI document.
With no controllers yet it'll be almost empty, and that's fine. The point is that the plumbing works.

**Try it:** open `http://localhost:8080/swagger-ui.html` in a browser. 🎉

---

## Part 7: problem+json (a standard way to say "something went wrong") ⚠️

### The problem
Without a standard, every API invents its own error shape:
```json
{ "error": "not found" }                         ← team A
{ "msg": "NOT_FOUND", "code": 404 }              ← team B
{ "timestamp": "...", "status": 404, "path": ... } ← Spring's default
```
Then I (frontend) have to write a different `if` for every shape. 😩

### The fix: RFC 9457 "Problem Details"
An **official internet standard** for error responses. Every error looks the same:
```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No static resource does-not-exist.",
  "instance": "/does-not-exist"
}
```
- `type` = a URI that identifies *the kind* of error (later: e.g. `.../errors/insufficient-funds`)
- `title` = a short human summary
- `status` = the HTTP status code
- `detail` = what happened *this time*
- `instance` = which request/URL it was about

Notice the special **`Content-Type: application/problem+json`**. That's how clients know "this is a standard error".

**Why now, with no features yet?** Because if the error format is decided on day 1, every future error (insufficient funds,
duplicate transfer, wallet frozen...) automatically follows it. Changing the format later = breaking every client. 💥

**What the ticket wants:** AC 3 says `GET /does-not-exist` → `404` + `Content-Type: application/problem+json`.
Spring Boot has built-in support (your `application.yml` already turns on `problemdetails`). **But don't trust it, test it:**
```
curl -i localhost:8080/does-not-exist
curl -i -H "Accept: text/html" localhost:8080/does-not-exist
```
Look at the `Content-Type` header in both. Are they the same? Is that OK? 🤔 Bring your answer to the review.

**One security thought:** an error response must never contain stack traces, SQL or internal class names. Attackers love those.

---

## Part 8: heap size (how much memory the JVM gets) 🧠

### Quick refresher
- **Heap** = where all your Java objects live. `-Xmx512m` means "the heap can grow up to 512 MB".
- If you set **nothing**, the JVM guesses. Inside a container it takes **25% of the container's memory** by default
  (look up `MaxRAMPercentage`).

### Why the ticket says "set it explicitly, no defaults"
Because the guess is how real production outages happen. Two classic stories:

```
😱 Story 1: heap too BIG for the container
   container limit: 1 GB,  -Xmx1g
   → heap (1 GB) + metaspace + threads + native memory > 1 GB
   → Kubernetes kills the pod: "OOMKilled, exit code 137"
   → it restarts, fills up, gets killed again... CrashLoopBackOff

😴 Story 2: heap too SMALL (often the 25% default)
   container limit: 2 GB, but the default gives 512 MB of heap
   → GC runs constantly, latency goes up, and you paid for 1.5 GB of memory you never use
```

**Remember:** the JVM uses memory *outside* the heap too: metaspace (loaded classes), thread stacks (~1 MB per thread),
direct buffers, the JIT compiler... So **heap ≠ total memory.** The heap must leave room for everything else.

### What the ticket wants
In the design: **one number, and the reason for it.** For example (made up, not your answer!): *"Heap X MB because the container will have Y MB,
and I'm leaving Z MB for non-heap because..."*

Questions to think about:
- Our whole stack is capped at 6 GB on your laptop (see WAL-0 and Known risks in `PROJECT.md`). How much of that should the backend get?
- Do you set a fixed `-Xmx`, or a percentage (`-XX:MaxRAMPercentage`)? Which one survives someone changing the k8s memory limit later?
- Where do you set it so it applies to `bootRun`, tests *and* later the Docker image?
- How do you **prove** the setting took effect? (Hint: Actuator metrics, or `jcmd <pid> VM.flags`.)

We'll do an `OOMKilled` incident drill later. This decision is exactly what it will test. 😈

---

## Part 9: "starts in under 20 seconds" ⏱️

**Why the ticket cares:**
- During a **rolling deploy**, Kubernetes starts a new pod and waits until it's ready before killing an old one. Slow startup = slow, risky deploys.
- When traffic spikes, new pods have to come up *fast* to help.
- If startup is slower than the k8s **startup probe** allows, k8s thinks the app is broken and kills it, *forever*. (Another future incident drill 😉)

An empty Spring Boot app should start in a few seconds. 20 s is a generous upper limit.
**Write down your actual number** (it's in the last startup log line: `Started BackendApplication in X seconds`).
It's your baseline. When it creeps up later, you'll notice.

---

## Part 10: the runbook 📖

**What it is:** a short "how to operate this thing" note, written for the person on call at 3 a.m. (often future-you, half asleep 😴).

For WAL-0.1 it covers:
- how to build: `./gradlew build` (and what you need first: JDK? Docker?)
- how to run locally (which option from the ticket you chose)
- how to add a new migration
- what to do when Flyway says a checksum doesn't match

Rule of thumb: **if a step is only in your head, it isn't done.** Copy-pasteable commands, no essays.

---

## Part 11: how the ticket checks all this (the acceptance criteria) ✅

| AC | Plain English | How you prove it |
|---|---|---|
| 1 | Everything builds and the tests pass, including the Docker test | `./gradlew build` → green |
| 2 | The walls between modules really work | break a wall on purpose → test goes red → screenshot or paste in the PR |
| 3 | Errors use the standard format | `curl -i localhost:8080/does-not-exist` |
| 4 | The API "menu" works | open `/v3/api-docs` |
| 5 | Fast startup | the `Started ... in X seconds` log line |
| 6 | Flyway and Testcontainers rules F1–F6 and T1–T6 | your test + config |
| 7 | Flyway really refuses an edited old migration | the error pasted into the PR (needs a DB that keeps its data, see the ticket) |

---

## Part 12: what YOU do next (the design doc) ✍️

Before writing *any* real code, you fill in
`docs/backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/design.md`.
Half a page to a page. Bullets are fine. Here's what I'll look for in each section:

**Approach:** 4–6 bullets: what you'll build and in which order.

**Decisions and alternatives:** for each one, *what you chose, what you rejected, and why*:
- [ ] packages + Modulith **vs** Gradle subprojects
- [ ] one schema **vs** one schema per module (and where `flyway_schema_history` goes)
- [ ] `@ServiceConnection` **vs** `@DynamicPropertySource`
- [ ] how one Testcontainers container is shared by every test class
- [ ] how you'll run the app locally before compose exists
- [ ] Kotlin DSL (or your pushback)
- [ ] the heap number and its reason

**Edge cases:** answer every one in the ticket, *including* "two pods run Flyway at the same time" and "Docker isn't running".

**Test plan:** which tests exist, and what each one *proves*. ("contextLoads" proves almost nothing. Say what *yours* proves.)

**Risks / questions:** anything you're unsure about. **Asking a good question here is a senior skill, not a weakness.**

### How I'll review it (so there are no surprises)
- ✅ A senior-level design: every choice has a *reason* and a *cost*. Alternatives were really considered.
- ⚠️ A mid-level design: right choices, but "because it's the default" or "because the tutorial did it".
- ❌ A junior-level design: no alternatives, edge cases skipped, "I'll figure it out while coding".

You've got this. 💪 Do the two learning exercises first, then write the design. Due **Wed 2026-11-25**.

---

## Mini glossary 📚

| Word | Friend definition |
|---|---|
| **Skeleton** | an empty but correctly structured app that every feature builds on |
| **Modular monolith** | one app, one deploy, strict walls between modules |
| **Module boundary** | the wall: only the "front door" classes are usable from outside |
| **Spring Modulith** | a library whose test checks that nobody broke a wall |
| **ADR** | a one-page "why we decided this" note |
| **Migration** | a versioned SQL file that changes the database (see flyway-basics) |
| **Testcontainers** | real throwaway Docker databases for tests (see testcontainers-basics) |
| **Actuator** | built-in endpoints like `/actuator/health`: the app's pulse |
| **OpenAPI / springdoc** | the auto-generated "menu" of your API, and the contract with the frontend |
| **problem+json (RFC 9457)** | the standard shape for error responses |
| **Heap / `-Xmx`** | the memory for Java objects, and its maximum size |
| **OOMKilled / exit 137** | Kubernetes killed your pod for using too much memory |
| **Runbook** | "how to run and fix this", written for the 3 a.m. on-call person |
