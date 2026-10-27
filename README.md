# CaseFlow Legacy
Existing Spring Boot REST API for internal incident management. This repository is a simulated legacy product used to practise real backend maintenance workflows: reading unfamiliar code, implementing scoped changes, writing tests and submitting reviewed pull requests.

## Technology
Java 21 · Spring Boot · Maven (JAR) · PostgreSQL · Flyway · JUnit 5 · GitHub Actions

## Local development
Prerequisites: Java 21, the included Maven Wrapper, Docker with Compose for PostgreSQL.
1. From the repository root, copy `.env.example` to `.env` (`cp .env.example .env`; PowerShell: `Copy-Item .env.example .env`). Set a local `DB_PASSWORD`; never commit `.env`.
2. Start PostgreSQL: `docker compose up -d --wait`. Its port is `127.0.0.1:54329` (`DB_PORT` in `.env`).
3. Run `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`). Flyway applies schema and synthetic seed migrations automatically. API: `http://127.0.0.1:8080/api/tickets`.
4. Run unit tests with `./mvnw test` (Windows: `.\mvnw.cmd test`). Run the build and PostgreSQL integration tests with `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`); Testcontainers requires a running Docker engine. The JAR is `target/caseflow-legacy-0.1.0-SNAPSHOT.jar`.
See [API contract](docs/api.md) and [runtime and testing notes](docs/operations.md). The unauthenticated simulation binds to loopback and is intended for local training only.

## Working agreements
Open a branch per ticket; keep changes scoped; add focused tests; describe impact, verification and risks in your PR. Do not commit secrets or personal data. Merges require review and explicit authorization.

## Scope
Internal incident registration, triage, assignment, state transitions and search. The product is deliberately imperfect; enhancements are tracked separately, not disclosed as a solution list in this README.

## License and visibility
Private educational simulation. No license granted for redistribution unless the owner decides otherwise.
