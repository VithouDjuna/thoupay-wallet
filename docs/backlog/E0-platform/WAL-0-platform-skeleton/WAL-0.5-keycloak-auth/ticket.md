# WAL-0.5: Keycloak realm + JWT auth + `/api/v1/me`

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.5-keycloak-auth` |
| Depends on | WAL-0.3 |
| Estimate | 6 h |
| Target done | Tue 2026-12-01 |

## Goal
Keycloak issues tokens, the API validates them and maps their roles, and both web apps can log in.

## Scope
- Realm `thoupay`, versioned in git as an import file and loaded automatically when Keycloak starts.
- Clients: `thoupay-api` (resource server), and `thoupay-customer-web` and `thoupay-admin-web` (public clients with PKCE,
  and redirect URIs for the Vite dev servers on `http://localhost:5173` and `http://localhost:5174`).
- Realm roles: `CUSTOMER`, `MERCHANT`, `OPS_ADMIN`, `FINANCE_ADMIN`, `AUDITOR`.
- Seed users: one per role, plus one user with **no** role. Their logins are documented in the runbook (local only).
- The API is an OAuth2 resource server and maps Keycloak roles to Spring authorities.
- `GET /api/v1/me` returns the caller's `sub`, username and roles.
- The 401 and 403 responses from Spring Security are `problem+json` too.
- OpenAPI documents the bearer-token scheme, and Swagger UI can call `/api/v1/me` with a token.

## Acceptance criteria
1. `GET /api/v1/me`:
   - no token → `401`, `problem+json`
   - valid `CUSTOMER` token → `200` with `sub`, username and `["CUSTOMER"]`
   - expired token, or a token signed by another realm → `401`
   - valid token for the user with no role → `200` with an empty role list (no 500, no NPE)
2. A test proves that an endpoint restricted to `OPS_ADMIN` returns `403` (`problem+json`) for a `CUSTOMER` token.
   Use a test-only endpoint or method security. Don't ship a fake admin feature.
3. The tests run either without a real Keycloak or with a Testcontainers Keycloak. Explain your choice.

## Edge cases (answer them in [design.md](design.md) before you write code)
- **Issuer mismatch:** the browser gets tokens from `http://localhost:8080/realms/thoupay`, but a backend in a container
  reaches Keycloak at `http://keycloak:8080`. The `iss` claim must match. How do you avoid getting a 401 with a valid token?
- **Clock skew:** the containers' clocks and the host's clock can differ when tokens are validated.
- **CORS:** the web apps run on a different port from the API.

## Definition of Done
- [ ] `design.md` approved (✅ in `review.md`) before the first code commit
- [ ] Acceptance criteria 1–3 pass
- [ ] OpenAPI spec updated
- [ ] Frontend integrated: login and `/me` in both apps. I do this and describe it in `frontend.md` in this folder
- [ ] Runbook section: how to get a token from the command line, and the usual causes of a 401
