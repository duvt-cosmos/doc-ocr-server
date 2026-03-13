# Doc OCR Server - Agent Instructions

This project is part of an AWS practice workspace, specifically a Spring Boot server for Document OCR (`doc-ocr-server`). 

## Context and Purpose
- **Project Name:** doc-ocr-server
- **Description:** A Java Spring Boot REST API for processing and tracking Document OCR tasks.
- **Environment:** Designed for AWS deployment, specifically interacting with AWS RDS (Postgres) and eventually SQS/S3.

## Tech Stack
- **Language:** Java 24
- **Framework:** Spring Boot (v4.0.2 / latest)
- **Database:** PostgreSQL accessed via `software.amazon.jdbc:aws-advanced-jdbc-wrapper:3.2.0`
- **Data Access:** Spring Data JPA & Spring JDBC
- **Build Tool:** Gradle
- **Boilerplate Reduction:** Lombok (`@RequiredArgsConstructor`, `@Data`, `@Slf4j`, etc.)
- **Containerization:** Docker & Docker Compose (`spring-boot-docker-compose`)

## Code Style & Architectural Guidelines

### 1. Spring Boot Architecture
- **Controllers:** `com.example.dococrserver.controllers` -> Keep controllers thin. They should primarily handle routing, parameter validation, and mapping responses.
- **Services:** `com.example.dococrserver.services` -> Place all business logic, OCR orchestrations, and AWS interactions (e.g., SQS publishing) here.
- **Repositories:** `com.example.dococrserver.repositories` -> Use Spring Data JPA Interfaces for DB interactions. Prefix custom queries or projections appropriately.
- **Models/Entities:** `com.example.dococrserver.models` -> Define JPA `@Entity` classes here.

### 2. General Java Practices
- **Lombok:** Always use Lombok for getters, setters, constructors, and logging to reduce boilerplate. Example: Use `@RequiredArgsConstructor` with `final` fields for Dependency Injection instead of `@Autowired`.
- **Modern Java:** Leverage Java 24 features (Record classes for DTOs, Pattern Matching, Switch expressions).
- **Error Handling:** Use global `@ControllerAdvice` for exception handling rather than scattering try-catch blocks in controllers.
- **Immutability:** Favor `final` variables and immutability where possible. 

### 3. AWS Integration Specifics
- This project leverages AWS Advanced JDBC Wrapper. Be mindful of multi-az failover capabilities when dealing with RDS.
- Cloud dependencies (`spring-cloud-aws-dependencies`) might be present. Assume standard AWS SDK v2 integrations.

### 4. Database Config
- Database properties are configured in `application.properties`. 
- Passwords and secrets might use environment variables. Prefer `application.yml` or `application.properties` with `${ENV_VAR}` interpolations for sensitive data.

## Building and Running
```bash
# Build
./gradlew clean build

# Test
./gradlew test

# Run Locally
./gradlew bootRun
```
