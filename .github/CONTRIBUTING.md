# Contributing

## One branch per ticket
- Branch: `feature/WAL-x.y-short-name`.
- Code **and** docs for that ticket go in the **same branch and the same PR**
  (ticket notes, screenshots, runbook, `docs/training/` updates).
- No separate branch or PR just for docs.

## Merging
- Squash merge only. The PR title becomes the commit message: `feat(WAL-x.y): ...`, `fix(WAL-x.y): ...`, `chore(WAL-x.y): ...`.
- `main` is protected: CI must be green and the branch must be up to date.
