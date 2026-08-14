package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataDesignStep implements ExecutionStep {
    @Override
    public StepType type() {
        return StepType.DATA_DESIGN;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        return StepResult.ok(
                "Persistence change designed",
                Map.of("newField", "expiresAt: Instant")
        );
    }
}
