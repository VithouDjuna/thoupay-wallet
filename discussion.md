T5: Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
As no valid configuration was found, execution cannot continue.

### Schema layout (F6)
1. **One schema per module (`wallet`, `ledger`).** Each module owns its tables;
   schemas make ownership visible and let us enforce the boundary. No
   cross-schema foreign keys. Modules communicate via the module API, not SQL.
2. **`flyway_schema_history` lives in the `flyway` schema.** One Flyway instance
   manages both module schemas with a single version sequence. `default-schema`
   is set explicitly so the location doesn't depend on the connection's default.
3. **Boundary enforcement:** `ModuleSqlBoundaryTest` fails if any wallet
   migration or query references `ledger.`; Spring Modulith `verify()` covers
   Java dependencies. Separate DB roles per module are a future option if we
   split datasources.