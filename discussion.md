# Sprint 0 Discussion: ThouPay Wallet

From: Tech Lead / PO / SRE (Claude)
Last updated: 2026-11-24
Status: **waiting for 1 answer (section 3) and 1 action from you (section 4)**

## How this file works
- This file always shows the **current state** of our discussion. After each conversation I rewrite it:
  decisions move to section 1 and open questions stay in section 3.
- Answer on the **✍️ Answer:** lines, or in chat. Either way, I copy the result here.
- Once everything is decided, the decisions move to `PROJECT.md` and this file is closed.

---

## 💬 My response to your last answers

**Q1 (git): done ✅.** I created an isolated project at **`C:\Users\thenc\thoupay-wallet`**.
It's outside `C:\Users\thenc\Project`, so it doesn't belong to your `easy-common` repo.
- I ran `git init` there, set `origin` to `https://github.com/VithouDjuna/thoupay-wallet.git`, and made one clean first commit.
- I copied `CLAUDE.md`, `PROJECT.md` and this file there and created the folder layout. **I did not push.**
- I did **not** touch `easy-common`, `simple-pipeline` or any other repo, as you asked. The old
  `project-stepup` folder is unchanged.
- ⚠️ One warning, which I'm reporting and not fixing: the parent repo `C:\Users\thenc\Project` still has `origin` pointing at
  `thoupay-wallet.git`, because you changed it earlier. **Don't `git push` from `C:\Users\thenc\Project`.** That would
  push your `easy-common` code into the ThouPay repo. When you have time, point it back yourself:
  `git -C C:\Users\thenc\Project remote set-url origin https://github.com/VithouDjuna/easy-common.git`

**Q2 (currency): your answer is ambiguous, and as tech lead I won't guess on a data-model question.**
"Keep the scope unchanged" can mean two different things. See Q2 below. It's a one-letter answer.

---

## 1. Decisions made

### Project and repo
| # | Topic | Decision |
|---|---|---|
| D1 | Name | **ThouPay Wallet** (`thoupay-wallet`), GitHub `VithouDjuna/thoupay-wallet` |
| D1a | Location | **`C:\Users\thenc\thoupay-wallet`**, a standalone git repo isolated from your other projects |
| D2 | Build tool | Gradle (Kotlin DSL) |
| D3 | Layout | Monorepo: `backend/` (yours), `frontend/` (mine), `infra/`, `docs/{decisions,postmortems,reviews,runbooks}` (created) |
| D4 | Branching | Trunk-based, `feature/WAL-<n>-<slug>`, squash-merge, Conventional Commits |
| D5 | Language | English for tickets, reviews and docs |

### Way of working
| # | Topic | Decision |
|---|---|---|
| D6 | Definition of Done | **Option A**: a minimal platform (Postgres, Keycloak, Vault, ELK, Prometheus, Grafana) is set up in Sprint 0, and the full DoD applies from ticket 1 |
| D7 | Backend reviews | Written to `docs/reviews/WAL-<n>-review.md` in the `[severity] file:line` format |
| D8 | Frontend changes (mine) | A branch for each change, plus `docs/reviews/FE-<n>-<slug>.md` explaining what changed, why, and what to learn. **You** read it and merge |
| D9 | Feedback style | Direct. I say so when work looks junior (confirmed) |
| D10 | Discussion log | This file is rewritten after each conversation to show the current state |

### Time
| # | Topic | Decision |
|---|---|---|
| D11 | Capacity | 2 h every weekday, up to 6 h per weekend day (about 22 h/week) |
| D12 | Milestone 1 | **Dec 31, 2026: Phases 0–2 done** (platform skeleton, wallet, ledger, safe transfers, first drill) |
| D13 | Public launch | Around **May 2027** |

### Your background (for training calibration)
| # | Topic | Answer |
|---|---|---|
| D14 | Used in production | Redis, Keycloak, Vault, but only as already set up by DevOps, without deep understanding |
| D15 | Incidents seen | Pods restarting. A tech lead found the root cause (memory leak, growing heap, busy threads) |
| D16 | Weak areas | All of them: concurrency, transactions, k8s, observability, security, system design |
| D17 | Testcontainers | Never used |

### Machine and tools
| # | Topic | Decision |
|---|---|---|
| D18 | Machine | Your laptop: 16 GB RAM, 12 CPUs, Windows 11 + WSL2 + Docker |
| D19 | Docker memory | No `.wslconfig`, so the WSL2 default (~8 GB) applies. You chose to just restart Docker. **Known risk**: the full stack may get OOM-killed, which could become a real drill |
| D20 | Compose | Profiles `core`, `observability`, `messaging`, with the Elasticsearch heap capped at 1 GB |
| D21 | JDK | JDK 21 through IntelliJ (`~/.jdks/openjdk-21.0.2`). Set `JAVA_HOME` for `jcmd`/`jstack` before the first drill |
| D22 | Tools later | `k3d` and Helm in Phase 5, `k6` in Phase 8. `gh` isn't needed |

### Frontend
| # | Topic | Decision |
|---|---|---|
| D23 | Apps | Two apps: `frontend/customer`, `frontend/admin` (separate Keycloak clients) |
| D24 | Stack | React + TS + Vite, TanStack Query, React Router, Tailwind + shadcn/ui, `openapi-typescript`, `oidc-client-ts` (PKCE) |
| D25 | Brand | ThouPay. I choose the look |
| D26 | API contract | RFC 9457 `problem+json`, pagination on every list endpoint, `Idempotency-Key` on every POST that moves money, money as string decimals, ISO-8601 UTC |

### Architecture (planning defaults)
| # | Topic | Decision |
|---|---|---|
| D27 | Auth timing | Keycloak and basic roles move into Phase 0/1. Phase 3 covers fine-grained roles, Vault rotation and rate limits |
| D28 | Early drills | JVM/DB-level drills on Docker Compose. Pod-level drills start after Phase 5 |
| D29 | Currencies | **Pending, see Q2** |

---

## 2. Deferred: these come back as required parts of a ticket design
You chose to learn these during the project. They are not optional, though: "I don't know yet" won't pass the design review when they come back.
| Topic | Comes back in |
|---|---|
| Balance model (derived vs. stored) and locking | WAL-2 design (ledger), becomes ADR-001 |
| KHR decimal places (0 or 2), only if KHR is in scope | WAL-1 design |
| Why Kafka + outbox instead of a DB queue | Phase 4 design |
| PII encryption (fields, keys, searching by phone) | the first ticket that stores phone/KYC data |
| SLO (99.9% isn't possible on one node) and the 200 TPS definition | Phase 5 and Phase 8 |
| Public demo abuse (open sign-up or demo accounts) | Phase 9 |
| Hosting, domain, SMTP, logging in production | Phase 9 |

---

## 3. 🛑 Open question (1 left; it doesn't block WAL-0, it blocks WAL-1)

### Q2. Currency: what does "keep the scope unchanged" mean?
`PROJECT.md` says "multi-currency and FX are out of scope", but you also said "USD, KHR". These two statements conflict, so one has to give way.

| Option | What it means | Impact |
|---|---|---|
| **A** | **USD only** for now. `PROJECT.md` stays exactly as written. KHR is added later as a change request | Simplest WAL-1. We still store a `currency` column (always `USD`) so adding KHR later doesn't need a painful migration |
| **B** | **USD + KHR from day one**, with my rules: one wallet per currency per user, no transfers between currencies, no FX, limits and fees per currency | More realistic for Cambodia, but more work in WAL-1 and WAL-2 (per-currency limits, fees, rounding). `PROJECT.md` changes from "multi-currency out of scope" to "multi-currency wallets in scope, FX out of scope" |

My recommendation as tech lead: **A**. You're new to most of this stack, and Milestone 1 is 5 weeks away. In a real company,
the PO would then bring KHR later as a mid-project change request, which is the kind of requirement change you should practice handling.

Write **A** or **B**:
> ✍️ Answer:

---

## 4. ⚡ Action for you: move to the new project folder
From now on, work in **`C:\Users\thenc\thoupay-wallet`**, not in `C:\Users\thenc\Project\project-stepup`.
1. Open `C:\Users\thenc\thoupay-wallet` in IntelliJ.
2. Start Claude Code **from that folder**, so my session reads the right `CLAUDE.md` and `PROJECT.md`.
3. Answer Q2 in `C:\Users\thenc\thoupay-wallet\discussion.md` (this file is copied there).
4. Once you've checked that everything is in the new folder, you can delete `C:\Users\thenc\Project\project-stepup` yourself. I won't delete it.
5. Push when you're ready: `git push -u origin master` (from the **new** folder only).

> ✍️ Answer (write `done` when moved):

---

## 5. Next steps
1. You answer Q2 and move to the new folder.
2. I move all decisions into `PROJECT.md`, add the memory sections, and close this file.
3. I give you **WAL-0: Platform skeleton**, and you send me a short design before you write any code.
