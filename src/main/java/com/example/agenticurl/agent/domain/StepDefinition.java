package com.example.agenticurl.agent.domain;

import com.example.agenticurl.agent.enums.StepType;

import java.util.List;

public record StepDefinition(
        String id,
        StepType type,
        String description,
        List<String> dependencies,
        ExecutionPolicy policy) {
}
