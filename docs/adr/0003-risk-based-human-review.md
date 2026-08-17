# ADR-0003: Use deterministic requirement analysis and risk-based human review

- **Status:** Accepted (prototype)
- **Scope:** Requirement interpretation and governance

## Context

The orchestration demo should show how ambiguous and higher-risk engineering changes receive more oversight without making governance dependent on non-reproducible LLM outputs.

## Decision

Use `IntentAnalyzer` to classify risk and ambiguity through explicit keyword heuristics. `PlanBuilder` associates a policy with each graph node; `GovernancePolicy` selects review modes:

- Medium/high-risk API design: review **before** execution.
- Data design: review **before** execution.
- High-risk delivery decision: review **after** execution.
- Ambiguous requests: clarification is required before planning.

Store approvals with workflow, plan version, step, and phase. The reviewer can approve or reject through the workflow API.

## Alternatives considered

- **Automatic approval of every node:** Lower friction but hides important human decision points.
- **Require review of every step:** Simple governance but demonstrates no risk differentiation.
- **LLM-based risk classification:** More flexible, but harder to test and reproduce in this prototype.

## Consequences

**Benefits**
- Policy decisions are deterministic and testable.
- Risk-sensitive review points are explicit in the execution graph.
- Review-after-execution can preserve completed work while awaiting final sign-off.

**Trade-offs and risks**
- Keyword heuristics cannot understand all real-world security and compliance contexts.
- Review APIs are not a complete identity/authorization system.
- Simultaneous review submissions need stronger atomicity safeguards.

## Revisit when

The system must handle real users, organization policies, authorization, or higher-stakes changes. At that point, introduce authenticated reviewer identity, enforce policy at transaction boundaries, and audit policy versions.

[ADR index](README.md) · [Architecture](../architecture.md)
