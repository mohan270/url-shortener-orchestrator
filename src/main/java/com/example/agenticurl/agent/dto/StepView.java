package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record StepView(
        @Schema(example = "1")
        int planVersion,

        @Schema(example = "api-design")
        String stepId,

        @Schema(example = "API_DESIGN")
        String type,

        List<String> dependencies,

        @Schema(example = "REVIEW_BEFORE_EXECUTION")
        String executionMode,

        @Schema(example = "STOP")
        String failureStrategy,

        int maxRetries,

        @Schema(example = "WAITING_REVIEW")
        String status,

        int attemptCount,
        boolean fallbackUsed,
        boolean superseded,
        String resultSummary,
        Instant startedAt,
        Instant completedAt) {

    public static StepView from(ExecutionStepEntity step) {
        return new StepView(
                step.getPlanVersion(),
                step.getStepId(),
                step.getType().name(),
                step.getDependencies(),
                step.getExecutionMode().name(),
                step.getFailureStrategy().name(),
                step.getMaxRetries(),
                step.getStatus().name(),
                step.getAttemptCount(),
                step.isFallbackUsed(),
                step.isSuperseded(),
                step.getResultSummary(),
                step.getStartedAt(),
                step.getCompletedAt()
        );
    }
}
