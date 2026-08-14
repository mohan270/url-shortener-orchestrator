# URL Shortener Orchestrator

Java 17 + Spring Boot prototype for the interview assignment: a working URL shortener plus a governed, stateful engineering-workflow orchestrator.

## Setup and run

If Gradle wrapper files are not present after extraction, generate the pinned Gradle 8.7 wrapper once:

```bash
./setup-wrapper.sh
```

Then run:

```bash
./gradlew clean test
./gradlew bootRun
```

Application: `http://localhost:8080`

## API documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The production OpenAPI contract includes the URL-shortener and engineering-workflow APIs. Demo fault-injection endpoints are hidden from the production API documentation.

## URL-shortener verification

Create a short URL:

```bash
curl -X POST http://localhost:8080/api/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://www.google.com"}'
```

Resolve it:

```bash
curl -i http://localhost:8080/<short-code>
```

Analytics:

```bash
curl http://localhost:8080/api/urls/<short-code>/analytics
```

## Engineering-workflow verification

Start the brownfield scenario:

```bash
curl -X POST http://localhost:8080/api/workflows \
  -H 'Content-Type: application/json' \
  -d '{
    "requirement": "Add expiration support to short URLs"
  }'
```

The workflow pauses when a governed human review is required. Use the returned workflow ID to inspect persisted state:

```bash
curl http://localhost:8080/api/workflows/<workflow-id>
curl http://localhost:8080/api/workflows/<workflow-id>/steps
curl http://localhost:8080/api/workflows/<workflow-id>/events
curl http://localhost:8080/api/workflows/<workflow-id>/approvals
curl http://localhost:8080/api/workflows/<workflow-id>/metrics
```

Approve a waiting step:

```bash
curl -X POST \
  http://localhost:8080/api/workflows/<workflow-id>/steps/<step-id>/review \
  -H 'Content-Type: application/json' \
  -d '{
    "decision": "APPROVED",
    "comment": "Reviewed and approved"
  }'
```

Replan after an upstream requirement change:

```bash
curl -X POST http://localhost:8080/api/workflows/<workflow-id>/replan \
  -H 'Content-Type: application/json' \
  -d '{
    "requirement": "Add URL expiration and preserve analytics"
  }'
```

## Deterministic recovery demo

Fault injection is isolated behind the Spring `demo` profile:

```bash
SPRING_PROFILES_ACTIVE=demo ./gradlew bootRun
```

Then:

```bash
curl -X POST http://localhost:8080/api/demo/workflows \
  -H 'Content-Type: application/json' \
  -d '{
    "requirement": "Add expiration support to short URLs",
    "faults": {
      "buildFailuresBeforePass": 1,
      "securityScannerUnavailable": true,
      "deliveryFailure": false
    }
  }'
```

This demonstrates bounded retry and fallback using the same orchestration engine as the production workflow API.

## Assessment artifacts

- `docs/architecture.md`
- `docs/scenarios/greenfield.md`
- `docs/scenarios/brownfield.md`
- `docs/scenarios/ambiguous.md`
- `docs/testing.md`
- `docs/risks-and-limitations.md`
- `docs/final-summary.md`
