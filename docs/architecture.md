# Architecture Overview

## Components

The prototype is a modular monolith with two top-level domains:

- `url`: URL creation, redirect resolution, expiration, custom aliases, analytics, persistence, and API documentation.
- `agent`: requirement understanding, planning, orchestration, governance, execution evidence, human review, recovery, replanning, and reliability metrics.

## Orchestration model

`WorkflowEngine` owns workflow lifecycle state. `IntentAnalyzer` normalizes requirements and identifies ambiguity. `PlanBuilder` converts the normalized requirement into an explicit versioned dependency graph. `GraphScheduler` loads persisted nodes and runs only nodes whose dependencies and governance gates are satisfied.

The graph covers impact analysis, API/data design, implementation proposal, test design, build verification, security validation, policy validation, documentation, and delivery readiness. Data design is added when the requirement implies persistence or expiration changes.

Independent runnable nodes execute concurrently with `CompletableFuture`. Downstream nodes wait until every declared dependency is `SUCCEEDED`, providing explicit synchronization rather than linear task chaining.

## Entry and exit gates

The entry gate is requirement clarity. Ambiguous requirements transition to `NEEDS_CLARIFICATION`, and no execution graph is created until a human supplies clarification.

The exit gate is delivery readiness. A delivery decision cannot run until build, security, policy, and documentation dependencies are satisfied. High-risk delivery uses review-after-execution so a human owns the final release decision.

## Stateful execution and decision lineage

Four persisted records preserve workflow state:

- `WorkflowEntity`: requirement, normalized requirement, risk, workflow status, current plan version, and automatic-replan count.
- `ExecutionStepEntity`: graph node, dependencies, execution policy, status, attempts, result data, timestamps, fallback flag, and superseded flag.
- `ApprovalEntity`: human decision scoped by workflow, plan version, step, and review phase.
- `ExecutionEventEntity`: audit events for requirement intake, planning, execution, reviews, replanning, compensation, safe-stop, and completion.

Successful node outputs are serialized and rehydrated into `ExecutionContext.sharedState` when execution resumes, preserving cross-stage context without rerunning completed nodes.

## Governance and controlled autonomy

Each node carries an `ExecutionPolicy` with an execution mode, failure strategy, and bounded retry count. `GovernancePolicy` determines human-review requirements from risk. `PolicyValidationStep` makes security, compliance, and change-control guardrails explicit in the execution graph and blocks requirements that attempt to bypass mandatory controls. `SecurityValidationStep` performs independent security validation.

Review decisions are persisted rather than stored in memory. Rejections stop the workflow. High-risk delivery executes once, persists its result, and then waits for final human approval.

## Recovery and replanning

Build verification has a bounded retry budget. If retries are exhausted and the node policy is `REPLAN`, the current plan is marked superseded, the plan version increments, and a new graph is generated. Automatic replanning is bounded to prevent loops; exhausting that budget produces `SAFE_STOP`.

Compensation is logical in this prototype: the proposed engineering change is discarded and not released. It is not a Git or infrastructure rollback.

## Observability and reliability metrics

`/events`, `/steps`, and `/approvals` expose execution lineage. `/metrics` reports success rate, retry count, compensation/rollback count, MTTR, and end-to-end latency from persisted workflow state.

## API/schema definition

Springdoc generates OpenAPI from the running application. Swagger UI is available at `/swagger-ui/index.html`, and the machine-readable specification is available at `/v3/api-docs`. The demo fault-injection endpoint is excluded from the production API documentation.

## Key decisions

- Persist execution nodes and approvals so review pauses and application restarts do not reset workflow state.
- Preserve earlier plan outcomes and mark them `superseded` rather than deleting or overwriting history.
- Put autonomy and failure behavior on each graph node rather than hard-coding one workflow-wide policy.
- Use bounded execution capabilities rather than fixed persona-style agents; the dependency graph determines which capabilities run and when.
- Keep fault injection behind a `demo` profile so failure testing does not leak into the production API contract.
