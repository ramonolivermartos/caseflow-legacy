# Runtime and verification

This is a local educational simulation with synthetic data, no authentication and no external integrations. Keep the API and database on loopback; do not expose it to public networks. No real operator or customer information belongs in this workspace.

## Configuration

Spring reads the root `.env` as Java properties. Docker Compose reads the same file. Use plain unquoted `KEY=value` lines; do not add shell `export` prefixes. Supported values: `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PORT`, optional `DB_URL` and `PORT` (API, default 8080). Environment variables override file properties. Do not commit `.env`.

PostgreSQL alone runs in Compose. `docker compose down` stops it and preserves its named volume. Flyway owns schema creation; Hibernate only validates. V1 creates tables and indexes; V2 loads four operators, eight incidents and their comment/history records. Restarting applies no duplicate seed. Use a dedicated training database, never a production database. Changing initial database credentials in `.env` does not change credentials already stored in a volume.

## Tests

`./mvnw test` runs JUnit/Mockito service tests without a database. `./mvnw clean verify` additionally runs `TicketApiIT`: a real HTTP server, Flyway and PostgreSQL through Testcontainers. Integration failures fail the build; they are not silently skipped when Docker is unavailable. The image is `postgres:17-alpine`.

For an existing **disposable, empty PostgreSQL test database**, integration tests can instead use environment variables `IT_DB_URL` (JDBC URL), `IT_DB_USER`, `IT_DB_PASSWORD`. Tests migrate that database and add synthetic tickets; never point them at important data. These variables affect integration tests only, not application runtime. This fallback allows validation where Docker cannot run.

PowerShell example after setting those variables securely:
```powershell
.\mvnw.cmd clean verify
```

The `Build and test` workflow runs verification on pushes to `main` and pull requests with Java 21. It requires no repository secrets. Maven Wrapper downloads Maven 3.9.16 from Maven Central. Internet is required for initial dependency/image downloads. No deployment or paid external service is configured.

## Layout

`operator/` holds reference operators; `ticket/` contains the ticket API, transactional service, mappings, persistence and search. `common/` translates expected errors. API DTOs are separate from JPA entities. Ticket changes and their audit entries commit together; optimistic versioning protects concurrent writes. `src/main/resources/db/migration/` is the append-only migration history.
