# Current discussion (2026-11-28)

## WAL-0.2 CI pipeline: all 3 ACs ✅, PR #10 merged as `944d346`
- AC 1 ✅ (merge blocked on red), AC 2 ✅ (PR #11, backend skipped), AC 3 ✅ (1m 2s → 45s, on `main`).
  AC 3 proof: "Entries: 6 restored (235Mb), 2 saved (98Mb)". Paste it into the chore PR body.
- Chore PR on branch `chore/WAL-0.2-cleanup`, worked one step at a time:
  - ✅ Test PRs closed, test branches deleted (GitHub + local).
  - ✅ Screenshots in the ticket folder: `ac2-frontend-skip.png`, `ac3-main-run.png` (renamed from `img.png`).
  - ✅ Ruleset export already allows only squash.
  - ✅ README with CI badge (new, short, code-only front page).
  - ✅ Repo tidy (2026-11-28): training files moved to `docs/training/` (PROJECT.md, discussion.md, explain_rules.md).
    Rule in CONTRIBUTING.md: docs go in the same branch/PR as the ticket's code, no docs-only PRs.
  - ⏳ Next: runbook section "CI is red but works on my laptop" + branch protection notes in `docs/runbooks/local-platform.md`.
  - ⏳ `.gitignore`: remove the duplicate lines.
  - ⏳ Commit, push, open PR with AC 3 proof, green build, squash merge.
- Then: WAL-0.3 (compose core), starting with prep.md + design chat.

## Learned in chat
- `cancel-in-progress`: groups = workflow + ref, so PR runs and `main` runs never cancel each other.
- Why `main` can go red when every PR was green: each PR was tested against the old `main`. Fix: "Require branches to be up to date".
