# WAL-0.3: Docker Compose, `core` profile

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.3-compose-core` |
| Depends on | WAL-0.1 |
| Estimate | 4 h |
| Target done | Sat 2026-11-28 |

## Goal
One command starts PostgreSQL, Keycloak and Vault locally, and the app connects to that PostgreSQL.

## Scope
- `infra/compose/docker-compose.yml` with these profiles:
  - `core`: PostgreSQL, Keycloak and Vault. They're created now and configured in WAL-0.4 and WAL-0.5.
  - `observability` and `messaging`: empty placeholders. WAL-0.6, WAL-0.7 and Phase 4 fill them in.
- Every container has a healthcheck and an explicit memory limit, and a named volume if it stores data.
- Image versions are pinned (no `latest`).
- Keycloak stores its own data in PostgreSQL. Decide: a separate database on the same Postgres instance, or a separate container?
- Local-only values, such as the Postgres and Keycloak admin passwords, come from an ignored `.env`. Commit a `.env.example`.

## Acceptance criteria
1. On a clean clone, after `docker compose --profile core up -d`, every container is `healthy` within 3 minutes.
2. The app from WAL-0.1 starts against this PostgreSQL, and Flyway runs.
3. Running `docker compose down` and then `up` keeps the data, and `down -v` resets everything. Both are in the runbook.
4. The `core` profile uses less than 2 GB of memory (`docker stats`).

## Edge cases (answer them in [design.md](design.md) before you write code)
- For daily work, does the app run in Compose or on the host from IntelliJ? Choose one and explain what changes
  (hostnames, ports, debugging, drills with `jcmd`).
- What happens when port 5432 or 8080 is already in use on your machine?

## Definition of Done
- [ ] `design.md` approved (✅ in `review.md`) before the first code commit
- [ ] Acceptance criteria 1–4 pass
- [ ] Runbook section: how to start, stop and reset, how to see one container's logs, and the usual failures
