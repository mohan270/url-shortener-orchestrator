# ADR-0002: Persist versioned workflow state and execution evidence

- **Status:** Accepted (prototype)
- **Scope:** Workflow persistence, audit, and replanning

## Context

Workflow execution can pause for clarification or human approval. A later HTTP request needs enough information to resume an existing plan without replaying successful steps. Replanning must not erase earlier execution history.

## Decision

Persist four kinds of workflow records using JPA:

1. `WorkflowEntity`: current requirement, status, risk, plan version, replan count.
2. `ExecutionStepEntity`: per-version graph nodes, policy, state, attempts, result JSON, timestamps.
3. `ApprovalEntity`: decision keyed by workflow, version, step, and approval phase.
4. `ExecutionEventEntity`: events describing decisions and execution outcomes.

When a plan changes, mark previous steps as superseded and increase the plan version instead of overwriting history. Reconstruct the shared execution context from successful, persisted step outputs when resuming.

## Alternatives considered

- **In-memory workflow map:** Simpler but loses state on restart and cannot reconstruct a paused workflow reliably.
- **Overwrite the plan on replanning:** Reduces storage, but destroys decision lineage.
- **Adopt a durable workflow engine:** Stronger operational semantics, but more complex than necessary for a controlled prototype.

## Consequences

**Benefits**
- Approval/resume flows have durable evidence.
- Prior plans remain visible for audit and demonstration.
- State transitions are inspectable through workflow endpoints.

**Trade-offs and risks**
- Persisted records do **not** imply safe recovery of interrupted `RUNNING` steps.
- Concurrent approval/replan requests need transaction and optimistic-locking safeguards.
- JSON result payloads require compatibility rules if output schemas evolve.

## Revisit when

Multiple application instances execute work concurrently, external side effects are introduced, or automatic recovery after process crashes is required.

[ADR index](README.md) · [Architecture](../architecture.md)
