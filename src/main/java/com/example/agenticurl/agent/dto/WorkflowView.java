package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record WorkflowView(
        @Schema(example = "e32ead05-d51a-4a27-a7a4-bc6315f393e4")
        UUID id,

        @Schema(example = "Add expiration support to short URLs")
        String requirement,

        @Schema(example = "Add expiration support to short URLs")
        String normalizedRequirement,

        @Schema(example = "WAITING_REVIEW")
        String status,

        @Schema(example = "HIGH")
        String riskLevel,

        @Schema(example = "1")
        int planVersion,

        @Schema(example = "0")
        int automaticReplanCount,

        @Schema(
                description = "Step currently waiting for human action; null when the workflow is not paused for review",
                example = "api-design",
                nullable = true
        )
        String waitingStepId) {

    public static WorkflowView from(WorkflowEntity workflow) {
        return new WorkflowView(
                workflow.getId(),
                workflow.getRequirement(),
                workflow.getNormalizedRequirement(),
                workflow.getStatus().name(),
                workflow.getRiskLevel() == null
                        ? null
                        : workflow.getRiskLevel().name(),
                workflow.getPlanVersion(),
                workflow.getAutomaticReplanCount(),
                workflow.getWaitingStepId()
        );
    }
}
