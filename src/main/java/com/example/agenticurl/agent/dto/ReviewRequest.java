package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.enums.ReviewDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @Schema(example = "APPROVED")
        @NotNull ReviewDecision decision,

        @Schema(example = "API contract reviewed and approved")
        String comment) {
}
