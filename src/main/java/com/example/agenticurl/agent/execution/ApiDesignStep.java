package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApiDesignStep implements ExecutionStep {
    @Override
    public StepType type() {
        return StepType.API_DESIGN;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        return StepResult.ok(
                "API contract designed",
                Map.of(
                        "createRequest", "url, optional expiresInDays, optional customAlias",
                        "expiredRedirect", "HTTP 410 Gone"
                )
        );
    }
}
