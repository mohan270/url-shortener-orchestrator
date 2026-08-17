# ADR-0001: Use a modular monolith with explicit domain boundaries

- **Status:** Accepted (prototype)
- **Scope:** Application structure and deployment

## Context

The demo needs a real URL-shortening API and a separate engineering-workflow domain. The objective is to demonstrate workflow design and governance without introducing infrastructure that distracts from those capabilities.

## Decision

Keep one Java 17 / Spring Boot application, with `url` and `agent` packages representing distinct domains. Both use the same Spring Data JPA/H2 runtime and are deployed as one process.

## Alternatives considered

- **Independent microservices:** More deployment isolation, but require service-to-service communication and additional operational infrastructure.
- **One undifferentiated application package:** Fewer files initially, but weaker ownership boundaries and harder-to-review code.

## Consequences

**Benefits**
- Simple local setup; integrated API testing is straightforward.
- Clear domain separation without premature network boundaries.
- A single repository allows cross-domain architecture and workflow demonstrations.

**Trade-offs**
- Domains share process resources and database infrastructure.
- The modular boundary is organizational rather than independently deployable.
- A failure in one process can affect both domains.

## Revisit when

The two areas need independent scaling, ownership, security boundaries, or deployment lifecycles. A split should be based on demonstrated requirements, not portfolio optics.

[ADR index](README.md) · [Architecture](../architecture.md)
