package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TestDesignStep implements ExecutionStep {
    @Override
    public StepType type() {
        return StepType.TEST_DESIGN;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        return StepResult.ok(
                "Tests designed",
                Map.of(
                        "tests",
                        List.of(
                                "creation succeeds",
                                "expired redirect returns 410",
                                "unknown code returns 404"
                        )
                )
        );
    }
}
