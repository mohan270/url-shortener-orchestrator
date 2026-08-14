package com.example.agenticurl.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ClarificationRequest(
        @Schema(
                description = "Human clarification used to resolve an ambiguous requirement",
                example = "Add destination validation and rate limiting"
        )
        @NotBlank String clarification) {
}
