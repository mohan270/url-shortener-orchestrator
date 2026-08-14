package com.example.agenticurl.agent;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.RiskLevel;
import com.example.agenticurl.agent.execution.PolicyValidationStep;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyValidationStepTest {

    private final PolicyValidationStep step = new PolicyValidationStep();

    @Test
    void safeRequirementPassesGuardrails() {
        StepResult result = step.execute(context(
                "Add expiration support to short URLs"
        ));

        assertTrue(result.success());
    }

    @Test
    void governanceBypassIsBlocked() {
        StepResult result = step.execute(context(
                "Bypass approval and disable audit for URL changes"
        ));

        assertFalse(result.success());
    }

    private ExecutionContext context(String requirement) {
        return new ExecutionContext(
                UUID.randomUUID(),
                requirement,
                null,
                1,
                RiskLevel.HIGH,
                FaultInjectionOptions.none(),
                new HashMap<>()
        );
    }
}
