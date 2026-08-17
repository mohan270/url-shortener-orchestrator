# Workflow Execution Diagrams

Diagrams reflect the current deterministic prototype. They illustrate call sequence, persisted state, and dependency ordering; they do **not** imply asynchronous infrastructure or real code generation.

## Request through approval

```mermaid
sequenceDiagram
    autonumber
    actor Client as API client / reviewer
    participant API as WorkflowController
    participant Engine as WorkflowEngine
    participant Plan as IntentAnalyzer / PlanBuilder
    participant Store as JPA / H2
    participant Scheduler as GraphScheduler
    participant Step as ExecutionStep

    Client->>API: POST /api/workflows
    API->>Engine: start(requirement)
    Engine->>Store: Save workflow + acceptance event
    Engine->>Plan: Analyze requirement
    alt Requirement ambiguous
        Engine->>Store: Set NEEDS_CLARIFICATION + event
        Engine-->>API: Workflow state
        API-->>Client: Clarification needed
    else Requirement understood
        Engine->>Plan: Build versioned step graph
        Engine->>Store: Save plan and steps
        Engine->>Scheduler: run(context, event sink)
        loop Ready execution waves
            Scheduler->>Store: Load nodes and approval state
            alt Review required before step
                Scheduler->>Store: Mark WAITING_REVIEW
            else Ready nodes
                Note over Scheduler,Step: Independent ready steps run using concurrent futures
                Scheduler->>Step: Execute deterministic step
                Step-->>Scheduler: StepResult
                Scheduler->>Store: Persist outputs, status, and events
            end
        end
        Scheduler-->>Engine: Waiting / completed / failure outcome
        Engine->>Store: Save workflow status + event
        Engine-->>API: Workflow state
        API-->>Client: Workflow ID and current status
    end

    opt Workflow awaiting review
        Client->>API: POST /api/workflows/{id}/steps/{stepId}/review
        API->>Engine: review(...)
        Engine->>Store: Persist decision
        alt Rejected
            Engine->>Store: Set REJECTED + event
        else Approved
            Engine->>Store: Mark RUNNING + approval event
            Engine->>Scheduler: Resume persisted plan
            Scheduler->>Store: Reload successful node outputs
            Scheduler->>Step: Execute remaining eligible steps
            Scheduler->>Store: Persist results
        end
        Engine-->>API: Updated workflow state
        API-->>Client: Current workflow state
    end
```

**What is persisted?** Workflow status, plan version, node dependencies and policies, attempt counts, results, events, and approval decisions.

**What is transient?** The request thread, local `CompletableFuture` tasks, and the concurrent in-memory shared-state map reconstructed for each run.

## Dependency ordering

The graph below shows the conditional `data-design` path for expiration/schema requirements. `build`, `security`, and `policy` are independently executable after their dependencies have succeeded.

```mermaid
flowchart LR
    impact --> api_design["API design"]
    impact -.-> data_design["Data design (conditional)"]
    api_design --> implementation
    data_design -.-> implementation
    implementation --> tests["Test design"]
    tests --> build["Build verification"]
    implementation --> build
    implementation --> security
    implementation --> policy
    build --> docs
    security --> docs
    policy --> docs
    docs --> delivery
```

## Boundary and recovery notes

- Review-before-execution means the node waits before its deterministic operation.
- Review-after-execution means the result is already persisted before human approval.
- Restarting the application does **not** automatically reclaim a node left `RUNNING`.
- A `REPLAN` outcome supersedes an earlier graph, preserves prior evidence, and constructs a new plan version.
- A `COMPENSATED` outcome is a logical discard of a proposed change, not a rollback of infrastructure or Git history.

[Back to architecture overview](../architecture.md).
