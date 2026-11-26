# WAL-0.2: CI pipeline (GitHub Actions)

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.2-ci-pipeline` |
| Depends on | WAL-0.1 |
| Estimate | 2 h |
| Target done | Fri 2026-11-27 |

## Goal
Every PR and every push to `main` is built and tested automatically, so no later sub-ticket can merge a broken build.

## Scope
- `.github/workflows/backend-ci.yml`: checkout, JDK 21, Gradle with caching, `./gradlew build` (unit and Testcontainers tests).
- The job runs only when `backend/**` or the workflow file changes.
- When the build fails, the test reports are uploaded as an artifact.
- Branch protection on `main`: the CI check must pass before a merge. Set it in the GitHub settings and describe it
  in the runbook. This is the only click allowed in WAL-0.

## Acceptance criteria
1. A PR with a failing test shows a red check and cannot be merged.
2. A PR that changes only `frontend/` or `docs/` doesn't run the backend job.
3. A second run with no dependency changes is clearly faster than the first, which shows the cache works. Write down both times.

## Edge cases (answer them in the PR description, under "Decisions")
- Testcontainers needs Docker. Does the GitHub runner have it, and which runner do you pick?
- What happens with a PR from a fork, and why will that matter for secrets later?

## Definition of Done
- [ ] Acceptance criteria 1–3 pass
- [ ] CI badge in the README
- [ ] Runbook section: what to do when CI is red but the build works locally
