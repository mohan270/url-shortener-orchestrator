# ADR-0004: Use policy-driven DAG scheduling with bounded local recovery

- **Status:** Accepted (prototype)
- **Scope:** Scheduling, step execution, and failure behavior

## Context

An engineering change has dependent and independent phases. Some tasks can run concurrently, while others cannot start until validation and approvals are complete. Failure behavior should be visible and constrained.

## Decision

Use a versioned directed acyclic graph of `StepDefinition` nodes. Each node declares dependencies and an `ExecutionPolicy` consisting of execution mode, failure strategy, and retry limit.

`GraphScheduler` loads persisted nodes, executes currently eligible nodes in a local `CompletableFuture` wave, persists each result, and repeats until it completes, pauses for review, replans, or stops.

`WorkflowEngine` translates scheduler outcomes into workflow state. Automatic replanning is capped at one replan. A compensation outcome discards the proposed change without claiming an actual rollback.

## Alternatives considered

- **Fixed sequential pipeline:** Easier implementation, but no demonstration of dependency-aware parallelism.
- **Unbounded autonomous retries/replanning:** More activity, but difficult to reason about and unsafe.
- **Distributed durable task queue / workflow platform:** Appropriate for production scaling and recovery, but beyond the prototype's infrastructure scope.

## Consequences

**Benefits**
- Dependencies, approval gates, and retries are inspectable rather than implicit.
- Independent validation branches can execute in parallel.
- Failure decisions have bounded behavior and persisted evidence.

**Trade-offs and risks**
- The scheduler uses the default executor; concurrency is not explicitly bounded per workflow.
- Steps run in-process and are not leased to durable workers.
- No automatic reclamation is implemented for crashed `RUNNING` steps.
- Concurrent workflow advancement and unexpected executor exceptions need stronger handling.

## Revisit when

Production concurrency limits, timeouts, cancellation, multi-instance execution, or side-effect-safe recovery become requirements. Introduce bounded executors, idempotency keys, leases/checkpoints, and atomic state transitions.

[ADR index](README.md) · [Architecture](../architecture.md)
