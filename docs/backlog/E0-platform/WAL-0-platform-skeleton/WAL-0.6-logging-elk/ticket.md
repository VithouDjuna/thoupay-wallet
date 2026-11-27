# WAL-0.6: Structured logs + trace IDs → ELK

| Field | Value |
|---|---|
| Parent | [WAL-0](../ticket.md) |
| Branch | `feature/WAL-0.6-logging-elk` |
| Depends on | WAL-0.3 |
| Estimate | 5 h |
| Target done | Thu 2026-12-03 |

## Goal
Application logs are JSON, carry trace IDs, and can be searched in Kibana.

## Scope
- The app logs JSON, using Spring Boot structured logging or a Logback encoder (your choice).
- Every log line written during a request carries `traceId` and `spanId` (Micrometer Tracing with the OpenTelemetry bridge).
- The `observability` profile gets Elasticsearch (heap capped at 1 GB), Kibana, and a log shipper of your choice.
- The Kibana data view is created automatically, not by hand.

## Acceptance criteria
1. Using one request's `traceId` (from a response header or a log line), you can find all of that request's log lines in Kibana.
2. Every log line contains at least the timestamp (UTC), level, logger, message, `traceId`, `spanId` and service name.
3. Logs never contain the raw `Authorization` header or a full JWT. A test or a manual check proves it.
4. Elasticsearch, Kibana and the shipper together use less than 2.5 GB of memory.

## Edge cases (answer them in [design.md](design.md) before you write code)
- How do logs get from the app to Elasticsearch (a file plus a shipper, a TCP appender, or container stdout)? If
  Elasticsearch is down, what happens to the app? Can logging block a request thread?
- Should the API return the `traceId` to the client, for example in a header, so users can quote it to support?

## Definition of Done
- [ ] `design.md` approved (✅ in `review.md`) before the first code commit
- [ ] Acceptance criteria 1–4 pass
- [ ] Runbook section: how to find all the logs of one failed request
