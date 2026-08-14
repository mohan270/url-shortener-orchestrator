package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DocumentationStep implements ExecutionStep {
    @Override
    public StepType type() {
        return StepType.DOCUMENTATION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        return StepResult.ok(
                "Engineering evidence generated",
                Map.of("artifact", "workflow-summary.md")
        );
    }
}
