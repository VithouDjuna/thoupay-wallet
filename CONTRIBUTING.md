# Contributing rules: ThouPay Wallet

These are the team's working rules. They come from the Sprint 0 decisions
(`docs/decisions/sprint-0-decisions.md`) and the backlog workflow (`docs/backlog/README.md`).
If a rule here and a decision record disagree, the decision record wins. Fix this file.

---

## 1. Branching strategy (decision D4)

**Trunk-based development.** `main` is the only long-lived branch. It must always build and pass the tests.

| Branch type | Pattern | Example | Who creates it |
|---|---|---|---|
| Trunk | `main` | `main` | — |
| Ticket | `feature/WAL-<n>-<slug>` | `feature/WAL-1-wallet-creation` | Developer |
| Sub-ticket | `feature/WAL-<n>.<m>-<slug>` | `feature/WAL-0.1-backend-skeleton` | Developer |
| Frontend change | one branch per change (D8) | `frontend/FE-1-login-page` | Tech lead (Claude) |
| Incident drill | `incident/<name>` | `incident/heap-leak` | SRE (Claude) |

Rules:
- `<slug>` is lowercase kebab-case, short, and matches the ticket folder name.
- Branch from the latest `main`. Keep branches short-lived (days, not weeks). Rebase on `main` if it moves.
- **One sub-ticket = one design = one branch = one PR.** Don't mix tickets in one PR.
- Never commit directly to `main`. After WAL-0.2, branch protection enforces this: CI must pass before a merge.
- Never merge an `incident/*` branch into `main`. It holds a planted fault.
- The local branch is still `master`. Rename it before the first push:
  `git branch -m master main`, then `git push -u origin main`.

## 2. Commits: Conventional Commits (D4)

Format: `<type>(<scope>): <summary>`, imperative mood, lowercase, no period, about 72 characters max.

| Type | Use for |
|---|---|
| `feat` | New behaviour |
| `fix` | Bug fix |
| `refactor` | Code change with no behaviour change |
| `test` | Tests only |
| `docs` | Documentation only |
| `chore` | Build, dependencies, tooling |
| `ci` | GitHub Actions workflows |
| `perf` | Performance improvement |

Scope = the module or area (`wallet`, `ledger`, `auth`, `infra`, `frontend`).
Add the ticket ID in the footer, or in the summary of the squash commit.

```
feat(wallet): create wallet on first login

Refs: WAL-1
```

Breaking API change: add `!` after the scope (`feat(api)!: ...`) and a `BREAKING CHANGE:` footer.

## 3. Pull requests and merging

- PR title = the squash commit message, so it also follows Conventional Commits and includes the ticket ID:
  `feat(wallet): create wallet on first login (WAL-1)`.
- **Squash-merge only.** One ticket = one commit on `main`.
- Delete the branch after the merge.
- The PR description links the ticket folder and lists how you tested it.
- The developer merges all PRs, including frontend PRs from Claude.

## 4. Ticket workflow

1. **Ticket.** The PO (Claude) writes `ticket.md` in `docs/backlog/E<n>-<epic>/WAL-<n>-<slug>/`.
   A big story is split into sub-ticket folders `WAL-<n>.<m>-<slug>/` (D10a).
2. **Design first.** You write `design.md` (data model, API, transactions, failure cases).
   **No code before the design review.**
3. **Design review.** The tech lead reviews it in `review.md`.
4. **Implement** on the feature branch, then open one PR.
5. **Code review** in the same `review.md`. Comment format:
   `[severity: blocker|major|minor|nit] file:line: issue → why it matters → hint`.
   All blockers and majors are fixed before the merge.
6. **Frontend integration** by Claude, documented in `frontend.md` in the ticket folder
   (or `docs/backlog/frontend-changes/FE-<n>-<slug>.md` if not tied to a ticket).

## 5. Definition of Done (every ticket)

- [ ] Tests (unit + integration with Testcontainers where it touches the DB)
- [ ] Metrics exposed (Actuator / Prometheus)
- [ ] Structured logs visible in Kibana
- [ ] Secrets in Vault, none in code or git
- [ ] OpenAPI spec updated (`/v3/api-docs`)
- [ ] Frontend integrated
- [ ] Short runbook note in `docs/runbooks/`

## 6. Secrets

- Never commit secrets. Before WAL-0.4, local-only values go in an ignored `.env`. Commit only `.env.example`.
- After WAL-0.4, secrets live only in Vault.
- Check before you push: `git grep -i -E "password|secret"` should find nothing real.

## 7. Incidents

- Claude plants the fault on an `incident/<name>` branch and gives you only the symptom.
- After you find the root cause, write a postmortem in `docs/postmortems/` (timeline, root cause, fix, prevention).

## 8. Repo hygiene

- Work only in `C:\Users\thenc\thoupay-wallet`. Never push from the old `C:\Users\thenc\Project` repo.
- `PROJECT.md` is the team memory. Claude updates it at the end of each session.
- `discussion.md` holds the current open discussion. It is rewritten each session, not appended.
