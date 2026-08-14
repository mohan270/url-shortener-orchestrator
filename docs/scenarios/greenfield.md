# Greenfield Scenario

## Requirement

`Build a URL-shortener API with analytics.`

## Decomposition

The planner creates impact analysis, API design, implementation, test design, build verification, security validation, policy validation, documentation, and delivery-readiness nodes. The requirement does not imply an expiration/schema change, so the optional persistence-design node is not added.

## Orchestration

Impact analysis runs first. API design is governed according to assessed risk. After implementation, test design, security validation, and policy validation are independent paths. Build verification waits for implementation plus test design. Documentation waits for build, security, and policy validation before delivery readiness.

## Validation

URL-shortener integration tests verify creation, the full configured short URL, redirect behavior, and analytics. Workflow tests verify dependency ordering and governance behavior.
