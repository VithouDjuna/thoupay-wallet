# Runbook: local platform and CI

> A runbook is a short "what to do when X happens" note. Follow the steps in order.

---

## CI is red but the build works on my laptop

**What it means:** CI found a difference between your laptop and a clean Linux machine. CI is usually right.
Don't rerun until it's green, and don't change CI to look like your laptop. Find the difference and fix the cause.

### Steps
1. **Find the failing step.** On GitHub, open the PR → **Checks** → `wallet_backend` → the red job → the red step.
   Read the first error line, not the last one.
2. **If tests failed, download the test report.** On the run page, scroll down to **Artifacts** → `test-reports`
   (kept for 7 days). Open `build/reports/tests/test/index.html` in a browser.
3. **Run the same command as CI, from a clean state:**
   ```bash
   cd backend
   ./gradlew clean build --no-daemon
   ```
   Use a new terminal **without** your local env vars (CI has none). If it's red now, fix it locally.
4. **Still green locally? Check the usual causes:**

| Error you see in CI | Cause | Fix |
|---|---|---|
| `./gradlew: Permission denied` | Git stored `gradlew` as not executable (Windows has no such flag) | `git update-index --chmod=+x backend/gradlew`, then commit |
| `/bin/sh^M: bad interpreter` | A script has Windows line endings | Add `*.sh text eol=lf` and `gradlew text eol=lf` to `.gitattributes` |
| `Connection refused` to the DB / a service | Tests used your local DB through `SPRING_DATASOURCE_*` env vars | Tests must use Testcontainers (`@FullContextTest`), never local services |
| `FileNotFoundException` / class not found | Upper/lower case in a file name (`Application.YML` vs `application.yml`) | Rename with `git mv` to the exact case |
| A date or time test fails | The runner uses UTC, your laptop uses Asia/Bangkok | Use a fixed `Clock` in tests |
| A test fails only sometimes | Timing (`Thread.sleep`) or test order | Wait for a condition, not a time; make each test clean up its own data |
| Library not found | Your `~/.gradle` has an old copy, the runner doesn't | Declare the dependency in `build.gradle` |
| Job stopped at 15 min | `timeout-minutes: 15` was hit (a hung test or container) | Find the stuck test in the log; don't raise the timeout first |

5. **Fix, push, and check that CI goes green.** Write one line in the PR about what the difference was.

### `main` is red but every PR was green
Each PR was tested against the old `main`. The ruleset turns on "branches must be up to date" to prevent this.
If it still happens: fix forward with a new PR right away. Nobody merges anything else until `main` is green.

---

## Branch protection on `main` (set by hand in GitHub)

These settings live in GitHub, not in the code, so they are written down here.
Source of truth: [protect-main.json](protect-main.json) (exported ruleset).
Where: GitHub → **Settings** → **Rules** → **Rulesets** → `protect-main`.

| Rule | Setting | Why |
|---|---|---|
| Target | default branch (`main`) | |
| Require a pull request | on, 0 approvals (solo project) | No direct pushes to `main` |
| Merge method | **squash only** | One clean commit per PR on `main` |
| Required status check | `verify` | The one check that sums up all jobs (`alls-green`). A skipped `backend` job counts as OK (frontend-only PR) |
| Branches must be up to date | on | Prevents "every PR green, `main` red" |
| Block force pushes | on | History on `main` can't be rewritten |
| Block deletion | on | `main` can't be deleted |
| Bypass list | empty | Nobody, including the owner, skips the rules |

### If you change a rule
1. Change it in GitHub.
2. Export it again: Rulesets → `protect-main` → **⋯** → **Export ruleset**.
3. Replace `docs/runbooks/protect-main.json` with the export and commit it.
