package com.example.agenticurl.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WorkflowMetricsView(
        @Schema(description = "Percent of terminal execution-step outcomes that succeeded", example = "100.0")
        double successRatePercent,

        @Schema(description = "Number of retry attempts beyond each step's first attempt", example = "1")
        long retryCount,

        @Schema(description = "Number of compensation/rollback events recorded for the workflow", example = "0")
        long rollbackCount,

        @Schema(description = "Mean time to recovery (MTTR) in milliseconds for steps that succeeded after retry or fallback", example = "42")
        long meanTimeToRecoveryMs,

        @Schema(description = "Elapsed workflow time from creation to the current/terminal state", example = "1250")
        long endToEndLatencyMs) {
}
