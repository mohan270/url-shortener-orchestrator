package com.example.agenticurl.agent.domain;

import com.example.agenticurl.agent.enums.ExecutionMode;
import com.example.agenticurl.agent.enums.FailureStrategy;

public record ExecutionPolicy(
        int maxRetries,
        FailureStrategy failureStrategy,
        ExecutionMode executionMode) {

    public static ExecutionPolicy automatic() {
        return new ExecutionPolicy(0, FailureStrategy.STOP, ExecutionMode.AUTO);
    }
}
