package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;

public interface ExecutionStep {
    StepType type();
    StepResult execute(ExecutionContext context);
}
