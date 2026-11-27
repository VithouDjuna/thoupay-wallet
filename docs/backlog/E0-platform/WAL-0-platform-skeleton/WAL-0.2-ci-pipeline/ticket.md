# WAL-0.2: CI pipeline (GitHub Actions)

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) (Platform skeleton) |
| Branch | `feature/WAL-0.2-ci-pipeline` |
| Depends on | WAL-0.1 (the backend must exist before a robot can build it) |
| Estimate | 2 h of coding, plus about 2 h of learning ([ci-basics.md](../../../../learning/ci-basics.md) + [prep.md](prep.md)) |
| Target done | Fri 2026-11-27 |

> **New to CI? That's expected.** Nobody is born knowing GitHub Actions. This ticket explains every word it uses.
> If a word is still unclear, check [Words used in this ticket](#words-used-in-this-ticket) at the bottom, or [docs/glossary.md](../../../../glossary.md).
> The order to work in: **this ticket → [ci-basics.md](../../../../learning/ci-basics.md) → [prep.md](prep.md) → [design.md](design.md) → code.**

---

## 1. The story in plain words

**CI (Continuous Integration)** is a robot that builds and tests your code on a clean machine every time you push.

**Why we need it now:** in WAL-0.1 the tests passed on your laptop, but only because your laptop had
`SPRING_DATASOURCE_*` environment variables set. Without them, `main` was red, and nobody noticed until the review.
A robot with **none** of your personal setup would have caught that in about 5 minutes.

**User story:**
As the **ThouPay team**, we want every PR (Pull Request) and every push to `main` built and tested automatically,
**so that** broken code can never be merged into `main` again, whoever wrote it.

```
 You push code / open a PR
          │
          ▼
 ┌────────────────────────┐   reads .github/workflows/backend-ci.yml
 │ GitHub Actions          │   "Did backend code change? Then run the job."
 └───────────┬────────────┘
             ▼
 ┌────────────────────────┐   a fresh, empty machine (the "runner")
 │ Runner                  │   1. download the code
 │                         │   2. install Java 21
 │                         │   3. ./gradlew build  (compile + all tests, including Testcontainers)
 └───────────┬────────────┘
             ▼
     ✅ green  or  ❌ red   ← shown on the PR as a "check"
             │
             ▼
 ┌────────────────────────┐
 │ Branch protection       │   "Red check? The Merge button stays disabled."
 └────────────────────────┘
```
The ticket starts at **You push** and ends at **the Merge button**. Each line of the scope below is one box in this picture.

---

## 2. Scope: what you build (and why each part exists)

| # | What | In plain words | Why it exists |
|---|---|---|---|
| S1 | `.github/workflows/backend-ci.yml` that does: checkout → JDK (Java Development Kit) 21 → Gradle with caching → `./gradlew build` | A recipe file that tells the robot: get the code, install Java, then build and run **all** tests (unit + Testcontainers) | This is the robot. Without the file, nothing runs |
| S2 | The job runs only when `backend/**` or the workflow file itself changes | If a PR only changes the frontend or the docs, don't waste 5 minutes building the backend | Saves time and machine minutes. **Watch out:** S2 and AC 1 can fight each other. [prep.md](prep.md) section A explains this |
| S3 | When the build fails, upload the test reports as an **artifact** (a file you can download after the run) | When a test fails in CI, you need to see *why*. The robot's machine is deleted after the run, so the report must be saved first | Without it, a red CI says "failed" and you can't see the details |
| S4 | **Branch protection** on `main`: the CI check must pass before a merge | A GitHub setting that disables the Merge button while the check is red | A red check that you can still merge is only a suggestion, not a gate. This is the **only** manual click allowed in WAL-0. Write it down in the runbook so it can be repeated |
| S5 | **Lesson from WAL-0.1:** CI must not set `SPRING_DATASOURCE_*` | The tests must start their own Postgres with Testcontainers, the same way they would on a new teammate's laptop | If CI sets those variables, it hides the exact bug from WAL-0.1 |

**Out of scope** (don't build these now): building Docker images, deploying anything, frontend CI, code coverage, security scanners.
They come in later tickets.

---

## 3. AC (Acceptance Criteria): how we know it's done

An AC is a test of the **result**, not of the code. "I wrote the YAML file" is not an AC. "A broken test blocks the merge" is.

| AC | What must be true | Why we ask for it | What a reviewer wants to see |
|---|---|---|---|
| 1 | A PR with a failing test shows a **red check** and **can't be merged** | Proves the gate really works, not only that the robot runs | A PR where you broke a test **on purpose**, its red check, and a disabled Merge button |
| 2 | A PR that changes only `frontend/` or `docs/` **doesn't run** the backend job | Proves S2 works | A docs-only PR, and what its checks area shows. Also check: **can it still be merged?** |
| 3 | A second run with no dependency changes is **clearly faster** than the first, which shows the cache works. Write down both times | Proves the cache works (S1). Without a cache, every run downloads all of Gradle's libraries again | Both run times (for example "run 1: 4 min 10 s, run 2: 1 min 50 s") + the log line that says the cache was restored |

---

## 4. Edge cases: answer them in [design.md](design.md) before you write code

An **edge case** is an unusual situation that the "normal" plan doesn't cover. Seniors think about these *before* coding,
because fixing them later costs more.

| # | Question | Why it matters | Where to learn it |
|---|---|---|---|
| Q1 | Testcontainers needs Docker. **Does the GitHub runner have Docker, and which runner do you pick?** | Your tests start a real Postgres in a container. If the runner can't run Linux containers, every test fails in CI, even though they pass on your laptop | [prep.md → Q1](prep.md#q1-testcontainers-needs-docker-does-the-github-runner-have-it-and-which-runner-do-you-pick) |
| Q2 | **What happens with a PR from a fork, and why will that matter for secrets later?** | A fork PR runs a *stranger's* code on *your* CI. Later tickets will store passwords (secrets) in CI. Can the stranger's code read them? | [prep.md → Q2](prep.md#q2-what-happens-with-a-pr-from-a-fork-and-why-will-that-matter-for-secrets-later) |

"I don't know yet. I tried X. I will find out by Y" is a **good** answer in a design. Guessing is not.

---

## 5. DoD (Definition of Done)
- [ ] `design.md` approved (✅ in `review.md`) **before** the first code commit
- [ ] AC 1–3 pass, with the proof pasted in the PR description
- [ ] CI badge in the README (a small ✅/❌ image that shows if `main` is green, with a link to GitHub's docs on "Adding a workflow status badge")
- [ ] Runbook section in `docs/runbooks/local-platform.md`: **"CI is red but the build works on my laptop: what to do"**
      (hint: [ci-basics.md Part 12](../../../../learning/ci-basics.md) is the starting point)
- [ ] The branch protection settings are written down in the runbook (because they were set by hand)
- [ ] Any test workflow (for example `hello.yml` from the prep experiments) is deleted before the PR

---

## Words used in this ticket
| Word | Plain meaning |
|---|---|
| **CI** (Continuous Integration) | A robot that builds and tests every push on a clean machine |
| **GitHub Actions** | GitHub's built-in CI system. It's free for public repos |
| **Workflow** | A YAML file in `.github/workflows/` that says *when* the robot runs and *what* it does |
| **Job** | A group of steps inside a workflow that runs on one machine |
| **Runner** | The machine that runs a job. GitHub creates a fresh one for each job and deletes it afterwards |
| **Check** | The ✅/❌ result of a job, shown on the PR |
| **Artifact** | A file the job saves before its machine is deleted, so you can download it later |
| **Cache** | Saved files (for example Gradle's downloaded libraries) that are reused in the next run to save time |
| **Branch protection** | GitHub rules for a branch, for example "no merge into `main` while a check is red" |
| **Fork** | Someone else's copy of your repo on GitHub. They can send you PRs from it |
| **Secret** | A password or token stored encrypted in the repo settings, which the workflow can read |
| **Runbook** | A short "what to do when X happens" note for whoever is on duty |
| **Testcontainers** | A Java library that starts real Docker containers (here: Postgres) during tests |
