package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PolicyValidationStep implements ExecutionStep {

    @Override
    public StepType type() {
        return StepType.POLICY_VALIDATION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        String requirement = context.requirement().toLowerCase();

        if (requirement.contains("bypass approval")
                || requirement.contains("disable audit")
                || requirement.contains("hardcode secret")
                || requirement.contains("store credentials")) {

            return StepResult.failed(
                    "Policy guardrail blocked the proposed change",
                    Map.of(
                            "securityPolicy", "BLOCKED",
                            "compliancePolicy", "BLOCKED",
                            "changeControlPolicy", "BLOCKED",
                            "reason", "Requirement conflicts with mandatory governance controls"
                    )
            );
        }

        return StepResult.ok(
                "Security, compliance, and change-control guardrails passed",
                Map.of(
                        "securityPolicy", "PASS",
                        "compliancePolicy", "PASS",
                        "changeControlPolicy", "PASS",
                        "planVersion", context.planVersion(),
                        "riskLevel", context.riskLevel().name()
                )
        );
    }
}
