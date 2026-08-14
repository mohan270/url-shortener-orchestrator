package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuildVerificationStep implements ExecutionStep {

    @Override
    public StepType type() {
        return StepType.BUILD_VERIFICATION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        int attempt = (int) context.sharedState()
                .getOrDefault("attempt:build", 1);

        if (attempt <= context.faultInjection().buildFailuresBeforePass()) {
            return StepResult.failed(
                    "Build verification failed",
                    Map.of(
                            "attempt", attempt,
                            "reason", "simulated validation failure"
                    )
            );
        }

        return StepResult.ok(
                "Build verification passed",
                Map.of(
                        "attempt", attempt,
                        "testsPassed", 8
                )
        );
    }
}
