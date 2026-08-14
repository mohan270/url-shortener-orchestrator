# Risks, Trade-offs, Assumptions, and Limitations

- Requirement analysis and engineering-step outputs are deterministic in this prototype. A production implementation can place an LLM/provider behind those reasoning boundaries without changing graph execution and governance semantics.
- The implementation step generates a reviewable change set rather than autonomously modifying the repository. Production execution would use an isolated Git branch/worktree with explicit commit and rollback controls.
- Compensation is logical: the proposed engineering change is discarded and not released. It is not a Git revert or infrastructure rollback.
- Parallel execution uses local `CompletableFuture`; it is not a durable distributed worker system.
- H2 is used for evaluator convenience. A production deployment would use a durable database such as PostgreSQL.
- A process crash while a step is `RUNNING` is not automatically retried because the prototype cannot prove whether an external side effect occurred. Production recovery needs idempotency keys, leases/checkpoints, or a durable workflow runtime.
- MTTR is measured from persisted step timing for retry/fallback recovery. A production MTTR implementation would additionally persist per-attempt failure and recovery timestamps across process restarts and plan versions.
- Automatic replanning is intentionally bounded to one automatic replan to demonstrate controlled autonomy and prevent infinite loops.
- The demo profile provides deterministic failure injection only for assessment/testing and is excluded from the production OpenAPI contract.
