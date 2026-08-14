package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.entity.ApprovalEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ApprovalView(
        int planVersion,

        @Schema(example = "api-design")
        String stepId,

        @Schema(example = "BEFORE_EXECUTION")
        String phase,

        @Schema(example = "APPROVED")
        String decision,

        String comment,
        Instant createdAt) {

    public static ApprovalView from(ApprovalEntity approval) {
        return new ApprovalView(
                approval.getPlanVersion(),
                approval.getStepId(),
                approval.getPhase().name(),
                approval.getDecision().name(),
                approval.getComment(),
                approval.getCreatedAt()
        );
    }
}
