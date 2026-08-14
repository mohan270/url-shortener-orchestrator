# Ambiguous Requirement Scenario

## Requirement

`Make the URL shortener more secure.`

## Requirement understanding

The requirement does not identify the intended security outcome. `IntentAnalyzer` asks whether the required control is rate limiting, authentication, destination validation, or another security control.

## Orchestration

The workflow transitions to `NEEDS_CLARIFICATION`. The requirement-clarity entry gate prevents graph creation and execution until a human supplies clarification.

## Validation

An integration test verifies that the ambiguous request stops before planning and that no execution nodes are persisted.
