# WAL-0.4: Vault secrets + health groups

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.4-vault-secrets-health` |
| Depends on | WAL-0.3 |
| Estimate | 5 h |
| Target done | Sun 2026-11-29 |

## Goal
The application gets its database credentials from Vault, not from config. The health endpoints correctly report
whether the app is alive and whether it is ready.

## Scope
### Vault
- Code or scripts configure Vault at startup, with no `vault kv put` typed by hand in a terminal.
- The app reads its DB username and password from Vault at startup (Spring Cloud Vault or equivalent).
- No credentials in `application*.yml`, in Java code, or in the image.

### Health
- Actuator liveness and readiness groups are enabled.
- Requirement from Ops: **`/actuator/health/liveness` must report `DOWN` when Postgres or Vault is unreachable,
  so that Kubernetes restarts the pod automatically.**

## Acceptance criteria
1. The DB password exists only in Vault and in the ignored `.env` that bootstraps it. `git grep -i -E "password|secret"`
   over `backend/` finds no real values.
2. With Vault stopped, the app's startup behaviour (fail fast or retry) matches what you wrote under "Decisions", and the log says why.
3. `/actuator/health/liveness` and `/actuator/health/readiness` return the groups you chose under "Decisions".
4. An integration test covers reading the secret from Vault. Testcontainers has a Vault module.

## Edge cases (answer them in the PR description, under "Decisions")
- **Bootstrap:** some secrets must exist before Vault can serve any, such as the Postgres superuser password and the Vault token.
  Where do they live locally, and how would that be different in production?
- **Dev mode:** Vault in dev mode loses its data when it restarts. Is that acceptable for this project?
- **Auth method:** how does the app authenticate to Vault (root token, app token, AppRole), and why?
- **Startup ordering:** what happens if the app starts before Vault is ready?

## Definition of Done
- [ ] Acceptance criteria 1–4 pass
- [ ] ADR in `docs/decisions/`: local secrets bootstrap (Vault mode, auth method, how secrets get in)
- [ ] Runbook section: what to do when Vault is sealed or empty and the app can't start
