package com.example.agenticurl.agent.domain;

import java.util.List;

public record ExecutionPlan(
        int version,
        List<StepDefinition> steps) {
}
