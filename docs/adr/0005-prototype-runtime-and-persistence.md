# ADR-0005: Use H2 and deterministic engineering steps for local evaluation

- **Status:** Accepted (prototype)
- **Scope:** Runtime, storage, and reproducibility

## Context

The repository is intended to be run locally with minimal prerequisites while still demonstrating persistent workflow state, REST APIs, and automated tests.

## Decision

Use Java 17, Spring Boot, Gradle, Spring Data JPA, and a file-backed H2 database. Implement engineering steps as deterministic Java components that return explicit evidence and simulated outcomes. Put fault-injection APIs behind the `demo` Spring profile.

Do not claim that generated proposals represent actual code modifications, CI builds, scanner results, or deployments.

## Alternatives considered

- **PostgreSQL with Docker Compose from day one:** Closer to production storage, but increases setup requirements.
- **In-memory database only:** Faster startup but does not retain state between normal local application restarts.
- **Real LLM, CI, and scanning integrations:** More realistic dependencies but harder to reproduce and troubleshoot.

## Consequences

**Benefits**
- The project runs with a JDK and the included Gradle wrapper.
- Test scenarios are reproducible.
- Local workflow data can persist across ordinary restarts.

**Trade-offs and risks**
- H2 console and schema auto-update are enabled in the current base configuration; it is not hardened for deployment.
- Deterministic steps are demonstrations, not actual external engineering operations.
- External authentication, authorization, rate limiting, and real scanner integrations are not implemented.

## Revisit when

Preparing a deployable reference system: use environment-specific configuration, PostgreSQL, migrations, secure secrets management, production authentication and authorization, and CI/CD.

[ADR index](README.md) · [Architecture](../architecture.md)
