# Architecture Decision Records

These ADRs explain **why the current prototype is structured as it is**. They capture implementation choices, alternatives, and consequences. They do not assert production readiness.

| Record | Decision |
| --- | --- |
| [0001](0001-modular-monolith.md) | Modular monolith with domain boundaries |
| [0002](0002-persist-versioned-workflow-state.md) | Persist versioned workflow state and evidence |
| [0003](0003-risk-based-human-review.md) | Deterministic planning with risk-based human review |
| [0004](0004-governed-step-execution.md) | Policy-driven local DAG execution and recovery |
| [0005](0005-prototype-runtime-and-persistence.md) | H2 and deterministic execution for the prototype |

**Status convention:** `Accepted (prototype)` means the decision describes code currently in the repository. It does not mean the design is approved for production deployment.

[Back to architecture overview](../architecture.md).
