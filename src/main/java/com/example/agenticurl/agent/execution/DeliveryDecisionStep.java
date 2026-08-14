package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeliveryDecisionStep implements ExecutionStep {
    @Override
    public StepType type() {
        return StepType.DELIVERY_DECISION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        if (context.faultInjection().deliveryFailure()) {
            return StepResult.failed(
                    "Delivery gate failed",
                    Map.of("reason", "simulated release blocker")
            );
        }

        return StepResult.ok(
                "Outcome is delivery-ready",
                Map.of("deliveryReady", true)
        );
    }
}
