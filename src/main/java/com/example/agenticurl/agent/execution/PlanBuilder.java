package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.*;
import com.example.agenticurl.agent.enums.*;
import com.example.agenticurl.agent.governance.GovernancePolicy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanBuilder {

    private final GovernancePolicy governancePolicy;

    public PlanBuilder(GovernancePolicy governancePolicy) {
        this.governancePolicy = governancePolicy;
    }

    public ExecutionPlan build(
            int version,
            IntentAnalysis analysis) {

        List<StepDefinition> steps = new ArrayList<>();

        steps.add(new StepDefinition(
                "impact",
                StepType.IMPACT_ANALYSIS,
                "Determine affected components and contracts",
                List.of(),
                ExecutionPolicy.automatic()
        ));

        steps.add(new StepDefinition(
                "api-design",
                StepType.API_DESIGN,
                "Design API changes",
                List.of("impact"),
                new ExecutionPolicy(
                        0,
                        FailureStrategy.STOP,
                        governancePolicy.apiDesignMode(analysis.riskLevel())
                )
        ));

        String normalized = analysis.normalizedRequirement().toLowerCase();

        if (normalized.contains("expiration") || normalized.contains("schema")) {
            steps.add(new StepDefinition(
                    "data-design",
                    StepType.DATA_DESIGN,
                    "Design persistence changes",
                    List.of("impact"),
                    new ExecutionPolicy(
                            0,
                            FailureStrategy.STOP,
                            governancePolicy.dataDesignMode()
                    )
            ));
        }

        List<String> changeDependencies = steps.stream()
                .filter(step -> step.type() == StepType.API_DESIGN
                        || step.type() == StepType.DATA_DESIGN)
                .map(StepDefinition::id)
                .toList();

        steps.add(new StepDefinition(
                "implementation",
                StepType.IMPLEMENTATION,
                "Generate a reviewable implementation change set",
                changeDependencies,
                new ExecutionPolicy(0, FailureStrategy.STOP, ExecutionMode.AUTO)
        ));

        steps.add(new StepDefinition(
                "test-design",
                StepType.TEST_DESIGN,
                "Generate tests from the normalized requirement",
                List.of("implementation"),
                ExecutionPolicy.automatic()
        ));

        steps.add(new StepDefinition(
                "build",
                StepType.BUILD_VERIFICATION,
                "Compile and execute validation tests",
                List.of("implementation", "test-design"),
                new ExecutionPolicy(2, FailureStrategy.REPLAN, ExecutionMode.AUTO)
        ));

        steps.add(new StepDefinition(
                "security",
                StepType.SECURITY_VALIDATION,
                "Perform independent security validation",
                List.of("implementation"),
                new ExecutionPolicy(0, FailureStrategy.STOP, ExecutionMode.AUTO)
        ));

        steps.add(new StepDefinition(
                "policy",
                StepType.POLICY_VALIDATION,
                "Validate security, compliance, and change-control guardrails",
                List.of("implementation"),
                new ExecutionPolicy(0, FailureStrategy.STOP, ExecutionMode.AUTO)
        ));

        steps.add(new StepDefinition(
                "docs",
                StepType.DOCUMENTATION,
                "Generate engineering evidence and documentation",
                List.of("build", "security", "policy"),
                ExecutionPolicy.automatic()
        ));

        steps.add(new StepDefinition(
                "delivery",
                StepType.DELIVERY_DECISION,
                "Determine whether the outcome is delivery-ready",
                List.of("docs"),
                new ExecutionPolicy(
                        0,
                        FailureStrategy.COMPENSATE,
                        governancePolicy.deliveryMode(analysis.riskLevel())
                )
        ));

        return new ExecutionPlan(version, List.copyOf(steps));
    }
}
