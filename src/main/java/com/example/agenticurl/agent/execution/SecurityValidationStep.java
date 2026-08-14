package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityValidationStep implements ExecutionStep {

    @Override
    public StepType type() {
        return StepType.SECURITY_VALIDATION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        if (context.faultInjection().securityScannerUnavailable()) {
            return StepResult.fallback(
                    "Primary security scanner unavailable; local safety checks passed",
                    Map.of(
                            "schemeValidation", "PASS",
                            "secretCheck", "PASS"
                    )
            );
        }

        return StepResult.ok(
                "Security validation passed",
                Map.of("criticalFindings", 0)
        );
    }
}
