package com.example.agenticurl.agent;

import com.example.agenticurl.agent.domain.ExecutionPlan;
import com.example.agenticurl.agent.domain.IntentAnalysis;
import com.example.agenticurl.agent.domain.StepDefinition;
import com.example.agenticurl.agent.enums.RiskLevel;
import com.example.agenticurl.agent.execution.PlanBuilder;
import com.example.agenticurl.agent.governance.GovernancePolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanBuilderTest {

    @Test
    void expirationRequirementAddsDataDesignDependency() {
        PlanBuilder builder = new PlanBuilder(new GovernancePolicy());

        ExecutionPlan plan = builder.build(
                1,
                new IntentAnalysis(
                        false,
                        "Add expiration support to short URLs",
                        List.of(),
                        RiskLevel.HIGH
                )
        );

        assertTrue(plan.steps().stream().anyMatch(step ->
                step.id().equals("data-design")
        ));

        StepDefinition implementation = plan.steps().stream()
                .filter(step -> step.id().equals("implementation"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of("api-design", "data-design"),
                implementation.dependencies()
        );

        StepDefinition policy = plan.steps().stream()
                .filter(step -> step.id().equals("policy"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of("implementation"),
                policy.dependencies()
        );
    }
}
