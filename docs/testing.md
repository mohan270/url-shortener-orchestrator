# Testing Approach

Run the complete suite with:

```bash
./gradlew clean test
```

The automated tests validate assessment-critical behavior:

- URL creation returns the configured full short URL; redirect and analytics work end to end.
- Generated OpenAPI contains the URL-shortener and workflow APIs.
- Ambiguous requirements stop before planning.
- Expiration requirements add the brownfield data-design dependency.
- The execution graph includes explicit implementation and policy-validation nodes, and policy-bypass requirements are blocked.
- Execution nodes, dependencies, policies, attempts, results, and plan versions are persisted.
- Human approvals are durable and execution resumes from existing state.
- Parallel branches synchronize through declared dependencies.
- The brownfield flow demonstrates bounded build retry and security fallback.
- Review-after-execution does not rerun the delivery step after approval.
- Manual upstream requirement changes create a new plan version while retaining superseded history.
- Retry exhaustion triggers one bounded automatic replan and then safe-stops when the budget is exhausted.
- Workflow reliability metrics are derived from persisted workflow state.
- The demo fault-injection endpoint is unavailable unless the `demo` profile is active.
- A convention test enforces one declared public business method per Spring service.
