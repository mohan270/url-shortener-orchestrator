package com.example.agenticurl.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StartWorkflowRequest(
        @Schema(
                description = "Engineering requirement to analyze and execute",
                example = "Add expiration support to short URLs"
        )
        @NotBlank String requirement) {
}
