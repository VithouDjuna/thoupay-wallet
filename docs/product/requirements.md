# ThouPay Wallet: Product requirements

> Owner: PO (Claude). Changes to requirements are made here, and the change is noted in the ticket that introduces it.

## Overview
A web-based digital wallet where users can hold a balance, top up, transfer money to other users,
and pay with QR codes. An admin back office lets operations staff review risky transactions,
freeze accounts, issue refunds, and audit every money movement.

**Goal of this project:** build a production-grade backend that shows senior-level skill in
business logic, correctness, security, infrastructure, observability, and incident handling,
and is publicly accessible as a live demo for my portfolio (LinkedIn / interviews).

**Who builds what**
- Backend, API design, infrastructure: me
- Web frontend (customer site and admin back office): Claude, integrating with my API


## Users and roles
| Role | Description |
|---|---|
| Customer | Registers, verifies identity (KYC), manages wallet, sends and receives money |
| Merchant | Receives QR payments, sees settlement reports |
| Ops Admin | Reviews flagged transactions, freezes/unfreezes accounts, handles disputes |
| Finance Admin | Issues refunds, views ledger and reconciliation reports |
| Auditor (read-only) | Views audit logs and reports, cannot change anything |


## Features

### Customer web app
- Sign up / log in (Keycloak), with optional 2FA
- KYC levels: unverified → basic → full (each level has different limits)
- Wallet dashboard: balance, wallet ID, daily/monthly limits used
- Top up from a (simulated) bank or card
- Transfer to another wallet (by wallet ID or phone), with fee quote and confirmation
- Pay merchant by QR code
- Transaction history with filters, pagination, and CSV export
- Notifications (email / in-app) for every money movement

### Admin back office
- Search users, wallets, and transactions
- Review queue for flagged transactions (approve / reject)
- Freeze / unfreeze wallets with a reason
- Refunds and reversals (require approval from a second admin: maker-checker)
- Ledger view and daily reconciliation report
- Full audit log: who did what, when, from where


## Core business rules
- **Double-entry ledger:** every money movement creates balanced debit and credit entries.
  Balance is derived from or reconciled against the ledger. Money is never created or lost.
- **Money type:** use `BigDecimal` / `NUMERIC(19,4)`, never `double`.
- **Currency:** USD only for now. Every wallet, ledger entry and money field still stores an ISO 4217
  currency code (always `USD` today), so another currency can be added later without a painful migration.
- **Idempotency:** every write request (top-up, transfer, payment) requires an `Idempotency-Key`.
  The same key returns the same result and never charges twice.
- **Limits by KYC level:** per-transaction, daily, and monthly limits. A transfer that exceeds a limit is rejected.
- **Fees:** configurable fee rules (flat, percentage, free under a threshold).
- **Risk checks:** transfers above a threshold, or many transfers in a short time, go to manual review.
- **Transaction states:** `PENDING → COMPLETED | FAILED | REVERSED`, plus `ON_HOLD` for review.
  Only valid state transitions are allowed.
- **Frozen wallets** cannot send money. Receiving is allowed or blocked based on the freeze reason.
- **Refunds** create new reversing ledger entries. Never update or delete old entries.
- **Concurrency:** two simultaneous transfers from the same wallet must never overdraw it.


## Non-functional requirements
- **Latency:** p95 under 200 ms for reads and under 500 ms for transfers
- **Availability:** 99.9% target, zero-downtime deployments
- **Correctness:** zero balance mismatches in the daily reconciliation
- **Security:** OWASP Top 10, no secrets in code or images, PII encrypted at rest, rate limiting on auth and transfers
- **Auditability:** every admin action and money movement is logged and traceable end to end
- **Scalability:** a transfer flow that is proven with load tests (target: 200+ transfers/sec on a small cluster)


## Out of scope (for now)
- Real bank or card integrations (simulated only)
- Native mobile apps (the web app is responsive)
- Multi-currency and FX (USD only for now; a possible later change request)

