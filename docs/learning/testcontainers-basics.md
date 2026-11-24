# Testcontainers, explained like a friend would 🍻

> For [WAL-0.1](../backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/ticket.md).
> Read [flyway-basics.md](flyway-basics.md) first, because this one builds on it. No rush, and ask me anything that doesn't click.

---

## Part 1: the problem

You write a test: *"save a wallet, read it back, check the balance."* That test needs a **database**. So which one?

Let's try the usual options and see why each one hurts:

**😬 Option 1: H2, the in-memory fake database**
It's like practising for a driving test in a video game. It *looks* similar, but it isn't the real car.
H2 pretends to be Postgres, but your Flyway files are Postgres SQL, and H2 chokes on parts of it.
Worse, the way it **locks rows** is different. Later you'll test *"two transfers at the same time can't overdraw a wallet"*.
H2 can say ✅ while real Postgres in production says 💥.

**😬 Option 2: one shared database that everybody uses**
Like a shared fridge in an office. Someone else's leftovers are always in there.
Your test expects 0 wallets and finds 37 that a teammate created. Flaky tests, angry people. And CI can't reach it anyway.

**😬 Option 3: mock the repository**
Then you're testing… your mock. 🙃 The bugs in a payment system live in the SQL, the constraints and the transactions, which are exactly what a mock skips.

**😬 Option 4: install Postgres on your laptop**
It works for *you*. Your teammate has a different version, CI has nothing installed, and old data from yesterday makes today's test fail.

**What we actually want:**
> A **real** Postgres, the **same version** as production, **completely empty** every time, that **anyone** can get with **zero setup**.

Sounds impossible? That's Testcontainers. 😎

---

## Part 2: quick Docker refresher (30 seconds)

- An **image** = a recipe, e.g. "Postgres 18.1". It's downloaded once.
- A **container** = a dish cooked from that recipe. A real running Postgres. You can cook as many as you want and throw them away.

```
image  postgres:18.1-alpine   ──cook──▶  container #1 (running Postgres)
                              ──cook──▶  container #2 (another one!)
```

If you can run `docker run postgres`, you already understand 80% of Testcontainers.

---

## Part 3: so what *is* Testcontainers?

It's a **Java library** that does the `docker run` **for you, from inside your test**.

Think of it as a **paper plate** 🍽️:
- You grab a fresh one right before eating (the test starts → a brand-new Postgres).
- It's a real plate that holds real food (real Postgres, not a fake).
- When you're done, you throw it away, and there's no washing up (the test ends → the container is deleted along with its data).

It's not a replacement for JUnit. JUnit still runs your tests; Testcontainers just gets the database ready for them.

---

## Part 4: what happens when you press "run test" ▶️

```
You press ▶ on a test
   │
   ├─ 1. Testcontainers → Docker: "Cook me a postgres:18.1-alpine, please"
   │        (first time ever? Docker downloads the image, which takes about 1 minute. After that, about 3 seconds.)
   │
   ├─ 2. Docker starts it and gives it a RANDOM door number (port), e.g. 54871
   │
   ├─ 3. Testcontainers WAITS until Postgres says "I'm ready" 🙋
   │        (a process that has started isn't necessarily ready yet. Testcontainers handles that for you.)
   │
   ├─ 4. Spring Boot starts and connects to localhost:54871
   │        └─ Flyway sees an empty database → runs V1, V2, V3 ... (remember the stamp card?)
   │
   ├─ 5. Your test runs against a real Postgres ✅
   │
   └─ 6. Done → the container is deleted. Poof, the data's gone. Next run starts fresh.
```

---

## Part 5: the random port (the one concept that trips everyone up) 🚪

Inside the container, Postgres listens on the normal port **5432**.
But on *your machine*, Docker gives it a **random free port**, like **54871**. Next run it might be **61203**.

**"Why not just use 5432?!"** Because:
- You might already have a Postgres on 5432 → 💥 the ports clash.
- CI runs 10 builds at the same time on one machine → they'd all fight over 5432.

Random ports mean nobody ever clashes. 👍

**But here's the catch:** normally you write this in `application.yml`:
```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/thoupay
```
With Testcontainers you **can't**, because you don't know the port until the container has started.

So *something* has to tell Spring, at runtime: *"hey, the database is at port 54871, and here's the username and password."*
Spring Boot gives you **two ways** to do that, and choosing one is part of your design (ticket requirement **T2**):

| Way | In friend terms | Look up |
|---|---|---|
| Service connection | "Spring, this container IS my database. Figure out the rest yourself." | `@ServiceConnection` |
| Dynamic properties | "Spring, let me tell you the URL, username and password myself." | `@DynamicPropertySource` |

Go read about both, pick one, and tell me *why* in your design. 😉

---

## Part 6: the cleaner nobody told you about 🧹 (Ryuk)

Run `docker ps` while a test is running and you'll see **two** containers, not one:

| Container | What it is |
|---|---|
| `postgres:18.1-alpine` | your test database |
| `testcontainers/ryuk` | **the cleaner** |

Say you hit the red ■ stop button in IntelliJ halfway through a test. Your code never got to "delete the container", so it would keep running and eating memory.
**Ryuk notices your test died and cleans it up for you.** Nice little guy. 🤖

---

## Part 7: one container or many? (this decides whether your tests take 5 s or 5 min) ⏱️

Each new Postgres takes about **3 seconds** to start. Now imagine you have 30 test classes:

| Approach | Startup cost |
|---|---|
| 🐌 A new database for **every test method** | 30 classes × 10 methods × 3 s = **15 minutes**. Please no. |
| 🚶 A new database for **every test class** | 30 × 3 s = **90 seconds** of just waiting |
| 🚀 **One database for the whole test run** | **3 seconds**, once |

The ticket asks for the 🚀 one (requirement **T3**). Look up the **"singleton container"** pattern.

**"But if every test shares one database, won't they mess with each other's data?"** 🤔
Yes! Great question, and it's a real trade-off. Common fixes: each test cleans up after itself, uses unique IDs, or rolls back its transaction.
You don't need to solve this now, because WAL-0.1 has no tables yet. Just write it under "Risks" in your design so future-you remembers.

**Bonus thing to look up:** *Spring test context caching.* Spring tries to reuse one started app across test classes.
If Spring keeps the app running but your database container gets thrown away, you get weird "connection refused" errors in the second test class.
Make sure the app and the database live just as long as each other.

---

## Part 8: see it with your own eyes (example *without* Spring)

This is the smallest possible example, just Testcontainers and plain Java, to show the idea.
⚠️ **This is NOT the WAL-0.1 answer.** Your real test uses Spring and must meet T2–T5. This one is just for learning.

```java
// Testcontainers 2.x. Check the import for your version, because packages changed from 1.x.
class PlainTestcontainersDemo {

    @Test
    void postgresReallyRuns() throws Exception {
        try (var postgres = new PostgreSQLContainer("postgres:18.1-alpine")) {
            postgres.start();   // 🍳 cook a fresh Postgres and wait until it's ready

            System.out.println(postgres.getJdbcUrl());
            // → jdbc:postgresql://localhost:54871/test    ← look at that random port!

            try (var conn = DriverManager.getConnection(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                 var rs = conn.createStatement().executeQuery("SELECT version()")) {
                rs.next();
                System.out.println(rs.getString(1));
                // → "PostgreSQL 18.1 ..."   ← it's REAL Postgres, not a fake
            }
        }   // 🗑️ end of try → the container is thrown away
    }
}
```
(I haven't run this snippet, so if the import doesn't compile, check the Testcontainers 2.x docs for the right package.)

---

## Part 9: before you start, check your machine 🪟

1. Is **Docker Desktop** running? (the whale icon 🐳 in your taskbar)
2. In a terminal: `docker run --rm hello-world` → you should see "Hello from Docker!"
3. Download the image ahead of time so your first test isn't slow: `docker pull postgres:18.1-alpine`
4. An error like *"Could not find a valid Docker environment"* almost always means **Docker Desktop isn't running**. Start it first, before you change any code.
5. If your laptop gets slow: Docker + Gradle + IntelliJ together are memory-hungry (WSL2 gets about 8 GB). Close Chrome tabs. 😅

---

## Part 10: let's play (15 minutes) 🛠️

1. **Run the example** from Part 8. Run it **twice** and compare the port in the URL. Different each time? That's Part 5 in action.
2. **Catch it running.** Put a breakpoint inside the `try`. While it's paused, run `docker ps`. Can you see both containers (postgres + ryuk)?
3. **Walk inside.** While it's still paused:
   `docker exec -it <postgres-id> psql -U test -c "SELECT 1;"` → you're inside the real test database! 🚪
4. **Kill it rudely.** While it's paused, hit IntelliJ's red ■ stop. Wait about 10 seconds and run `docker ps` again. Who cleaned up? (👋 Ryuk)
5. **Turn Docker off** (quit Docker Desktop) and run the test. **Read the error slowly.** Imagine you're a new teammate seeing that.
   Is it clear what to do? That's what ticket requirement **T5** is about.
6. **Turn Docker back on** and run your real Spring test, `ApplicationIntegrationTest`. Look through the log for Flyway saying something like
   *"Successfully applied N migrations"*. That's both your new tools working together. 🎉
7. Delete the demo test from step 1. It was just a toy.

---

## Cheat sheet

- **Testcontainers** = a Java library that starts a real, throwaway Docker database for your tests
- **Why** = real Postgres, same version as prod, empty every time, zero setup for anyone
- **Random port** = you can't hard-code the URL; Spring must be told at runtime (`@ServiceConnection` or `@DynamicPropertySource`)
- **Ryuk** = the cleaner that removes leftover containers
- **One container for the whole run** = fast tests (look up "singleton container")
- **Needs** = Docker Desktop running. That's it.

## Bring these to the design review 💬
1. `@ServiceConnection` or `@DynamicPropertySource`: which did you pick, and why? (T2)
2. How do you know for sure that only **one** container ran during `./gradlew test`? (Hint: `docker ps` while it runs.) (T3)
3. In step 5, was the error clear enough for a teammate? (T5)
4. When 30 test classes share one database, how do you stop them stepping on each other's data?

Further reading, once this feels comfortable:
- Spring Boot + Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Testcontainers for Java: https://java.testcontainers.org/
- The Postgres module: https://java.testcontainers.org/modules/databases/postgres/
