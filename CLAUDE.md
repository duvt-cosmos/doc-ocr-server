# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew clean build       # Build
./gradlew test              # Run tests (uses JUnit 5)
./gradlew bootRun           # Run locally
```

Tests use H2 in-memory database via `@ActiveProfiles("test")`. See `src/test/resources/application-test.properties`.

## Architecture Overview

**Spring Boot 4.0.2 + Java 24** REST API for Document OCR task tracking.

### Package Structure

| Package | Purpose |
|---------|---------|
| `controllers` | HTTP endpoints (thin routing, validation) |
| `service` | Business logic, transactions |
| `repository` | Spring Data JPA interfaces |
| `entity` | JPA `@Entity` classes |
| `config` | Configuration (SSH tunnel, datasources) |

### Key Components

- **DocOcrServerApplication** - Loads `.env` via `dotenv-java`, enables JPA auditing
- **BaseEntity** - Auditing base class (`createdAt`, `updatedAt`, `version`)
- **SshTunnelService** - SSH tunnel to RDS via bastion host (controlled by `SSH_ENABLED` env var)

### Database

PostgreSQL via AWS Advanced JDBC Wrapper (`software.amazon.jdbc:aws-advanced-jdbc-wrapper`). SSH tunneling configured in `application.properties` for local dev against remote RDS.

## Deployment

GitHub Actions (`.github/workflows/aws.yml`) builds Docker image and deploys to Amazon ECS. Uses OIDC for AWS auth (no access keys).

## Environment Variables

- `SSH_ENABLED` - Enable SSH tunnel (default: `false`)
- `DB_PASSWORD` - Database password (loaded from `.env`)

`.env` is gitignored - do not commit credentials.