# Flyway, explained like a friend would 🍻

> For [WAL-0.1](../backlog/E0-platform/WAL-0-platform-skeleton/WAL-0.1-backend-skeleton/ticket.md).
> Grab a coffee. No rush. If something here doesn't click, that's on me, not you. Ask and I'll explain it another way.

---

## Part 1: the problem (forget Flyway for a minute)

Imagine you and I build an app together. It has a database.

**Monday:** you create a table on your laptop:
```sql
CREATE TABLE customer (id BIGSERIAL PRIMARY KEY, name VARCHAR(100));
```

**Wednesday:** I need an email column, so I run this on *my* laptop:
```sql
ALTER TABLE customer ADD COLUMN email VARCHAR(255);
```

**Friday:** we deploy to production. Now the questions start:
- Does production have the `email` column? 🤷
- Did anyone tell *you* to add it on your laptop? 🤷
- A new teammate joins next week. Which SQL do they run, and in what order? 🤷
- Who ran what on production, and when? 🤷

Nobody knows. Each database drifted in its own direction, and that's how you get *"works on my machine"* and 2 a.m. production bugs.

**What we're missing:** one list of every database change, in order, that every database follows. That's all Flyway is.

---

## Part 2: the idea, explained with save points 🎮

Think about a video game with save points:

```
Save 1 ──▶ Save 2 ──▶ Save 3
(start)   (got sword) (beat boss)
```

You can't get to save 3 without going through 1 and 2. And anyone who plays from the start passes the same saves, in the same order.

**Flyway does exactly that for your database:**

```
V1 ──────────────▶ V2 ──────────────▶ V3
create customer    add email column   create order table
```

Each "save point" is a small **SQL file**. That file is what people call a **migration script**. That's the whole mystery solved:

> **A migration script = a `.sql` file with one change to your database, plus a version number.**

There's no magic and no Java. It's just SQL you could also type into pgAdmin.

---

## Part 3: where the files live and what they look like

They go in this folder (Flyway looks there by default):

```
backend/src/main/resources/db/migration/
├── V1__create_customer_table.sql
├── V2__add_email_to_customer.sql
└── V3__create_order_table.sql
```

Inside, it's just SQL:

**`V1__create_customer_table.sql`**
```sql
CREATE TABLE customer (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
```

**`V2__add_email_to_customer.sql`**
```sql
ALTER TABLE customer ADD COLUMN email VARCHAR(255);
```

(These are made-up examples to learn with, not ThouPay tables.)

### The file name matters a lot
```
V2__add_email_to_customer.sql
│ │  └── a description for humans (underscores become spaces)
│ └──── TWO underscores. Yes, two. 👀
└────── "V" + number = the version. Flyway runs them in number order.
```

⚠️ **The classic trap:** with a single underscore (`V2_add_email.sql`), Flyway **ignores the file silently**: no error, nothing.
You'll stare at the screen wondering why your column isn't there. Everyone hits this once.

---

## Part 4: what Flyway actually does when the app starts

Flyway is like a **checklist with a stamp** ✅.

The first time it runs, it creates a small table in your database called **`flyway_schema_history`**. Think of it as the stamp card:
*"which save points has this database already passed?"*

Every time your Spring Boot app starts, Flyway goes through this:

```
1. "What files do I have?"            → V1, V2, V3
2. "What does the stamp card say?"    → V1 ✅  V2 ✅  (V3 missing)
3. "OK, run only V3."                 → runs V3, stamps it ✅
4. "Done, the rest of the app can start."
```

And the stamp card looks like this (simplified):

| version | description | checksum | success |
|---|---|---|---|
| 1 | create customer table | 12345678 | true |
| 2 | add email to customer | 87654321 | true |
| 3 | create order table | 55555555 | true |

### Why that's so nice
The same files give the same result everywhere:

| Whose database | What Flyway runs |
|---|---|
| A new teammate with an empty DB | V1, V2, V3 (everything, in order) |
| Your laptop, which already has V1 and V2 | only V3 |
| Production, which already has everything | nothing. "All good, carry on." |
| A test database (brand new every time) | everything |

Nobody runs SQL by hand anymore, and nobody has to remember anything. 🙌

---

## Part 5: the golden rule — never edit an old file 🔒

See the `checksum` column? A checksum is a **fingerprint** of the file's content. Change even one character and the fingerprint changes.

Think of it like a **sealed envelope**. Once V1 has run, Flyway has "sealed" it by remembering its fingerprint.

So if you go back and change V1:

```
Flyway: "Wait... the V1 file has fingerprint 99999999,
         but the stamp card says V1 was 12345678.
         Somebody changed history. I refuse to start. 🛑"
```

**Why so strict?** Because production was built from the *old* V1. If Flyway accepted the new one, your files would no longer describe
your real database. That's the drift from Part 1 coming back.

**So what do you do if V1 had a mistake?**
👉 Leave V1 alone and **add a new file** `V4__fix_the_thing.sql`. Always move forward, like Git commits: you don't rewrite a pushed commit, you add a new one.

(On your own laptop you *can* cheat by throwing your database away and starting fresh. On a shared database or production, never.)

---

## Part 6: "but at work Hibernate just creates the tables for me?"

Yep! That's `spring.jpa.hibernate.ddl-auto=update`. Hibernate looks at your `@Entity` classes and changes tables to match. It feels convenient. Here's why we don't do that with money:

| | Hibernate `update` 😬 | Flyway 😎 |
|---|---|---|
| Who decides what the tables look like | Hibernate *guesses* from your Java classes | **You**, in plain SQL |
| You rename a field | Hibernate adds a *new empty column*. The old data stays in the old column. | You write exactly the rename you want |
| You delete a field | The column stays forever | You decide |
| Can someone review it in a PR? | No, it happens invisibly at startup | Yes, it's a file in Git |
| Something doesn't match | Hibernate quietly changes **production** | The app refuses to start and tells you |

So the job split is:
- **Flyway** = the builder. It's the only one allowed to change tables.
- **Hibernate** = the inspector. It only *checks* that your entities match the tables (`ddl-auto=validate`). It never builds anything.

---

## Part 7: one new word, "schema"

You'll need this for your design. In PostgreSQL:

```
Database  "thoupay"                 ← the building
 ├── Schema "wallet"                ← a room in the building
 │     └── tables: wallet, ...
 ├── Schema "ledger"                ← another room
 │     └── tables: entry, ...
 └── Schema "public"                ← the default room everyone starts in
```

A **schema** is just a *folder for tables* inside one database.

Your design question is: **one shared room (`public`) for every module, or one room per module?**
- One room is simpler, but then what stops the `wallet` code from peeking at `ledger` tables?
- One room per module gives cleaner walls, but who creates the rooms, and where does the stamp card (`flyway_schema_history`) go?

There's no single right answer. I want to hear *your* reasoning. 😉

**And your V1 for WAL-0.1:** there are no business tables yet. V1 only sets up the rooms you choose. That's it.

---

## Part 8: let's actually do it (10 minutes) 🛠️

Reading only gets you so far. Breaking things teaches more. Do this in your current spike code (the code already in `backend/`):

1. **Create your first save point.** Make the file `src/main/resources/db/migration/V1__hello.sql` with:
   ```sql
   CREATE TABLE hello (id INT);
   ```
2. **Run your Testcontainers test** and put a breakpoint somewhere so it pauses (the database stays alive while it's paused).
3. **Look at the stamp card.** In a terminal:
   ```
   docker ps                                        ← find the postgres container ID
   docker exec -it <container-id> psql -U test      ← use your container's username if it isn't "test"
   SELECT * FROM flyway_schema_history;
   ```
   🎉 You should see your V1 row. That's Flyway's memory.
4. **Add a second save point:** `V2__hello_name.sql` with `ALTER TABLE hello ADD COLUMN name TEXT;` and run again. Now the card has V1 *and* V2.
5. **Break the golden rule on purpose.** Change the SQL in V1 and run again. Flyway gets angry. **Read the error and save it**, because
   the ticket (AC 7) wants it in your PR.

   > Wait, my test database is brand new every run, so why would it complain? 🤔 Good catch! In a fresh test DB it *won't*.
   > To see the error, you need a database that **keeps** its data between runs, like a local Postgres or the compose one from WAL-0.3.
   > Note down *why* you didn't see it in the test. That's a great thing to bring to the review.
6. **Fall into the classic trap:** create `V3_oops.sql` (one underscore). Run it. Did anything happen? Nope. Silent. Now you'll never forget. 😄
7. **Clean up:** delete the `hello` files. They were just for practice.

---

## Cheat sheet (keep this one in your head)

- **Migration script** = a `.sql` file with one database change, named `V<number>__<description>.sql`
- **Folder** = `src/main/resources/db/migration`
- **Flyway** = runs the missing scripts, in order, when the app starts
- **`flyway_schema_history`** = the stamp card of what has already run
- **Checksum** = the fingerprint. Never edit a script that already ran; add a new one.
- **Hibernate** = inspector only (`validate`), never builder

## Bring these to the design review 💬
1. In step 5, why didn't the test database complain? What does that tell you about testing migrations?
2. If V1 had a bug and had already reached production, what exactly would you do?
3. One room or one room per module? Why?

Further reading, once this feels comfortable (not before 🙂):
- Spring Boot + Flyway: https://docs.spring.io/spring-boot/how-to/data-initialization.html
- Flyway docs: https://documentation.red-gate.com/flyway
