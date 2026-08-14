# Brownfield Scenario

## Requirement

`Add expiration support to short URLs.`

## Impact reasoning and decomposition

`ImpactAnalysisStep` identifies the affected URL entity, DTO, services, controller, API routes, and create/redirect/analytics data flows. `PlanBuilder` adds both `api-design` and `data-design` after impact analysis. The implementation node depends on both design nodes.

After implementation, test design, security validation, and policy validation can proceed on independent branches. Build verification depends on implementation plus test design. Documentation waits for build, security, and policy validation before delivery readiness.

## Orchestration and governance

Expiration is classified as high risk. API and persistence design therefore require review before execution. The policy node validates security, compliance, and change-control guardrails. Build verification has two bounded retries with `REPLAN` as its failure strategy. High-risk delivery uses review-after-execution so the automated delivery assessment runs once and a human owns the final decision.

## Validation and recovery evidence

The `demo` profile can deterministically inject one build failure and make the primary security scanner unavailable. This demonstrates retry and fallback without changing the production API. Integration tests verify persisted node state, durable approvals, retry count, fallback usage, final review, automatic replanning after retry exhaustion, plan-version lineage, and safe-stop when the replan budget is exhausted.
