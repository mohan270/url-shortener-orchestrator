package com.example.agenticurl.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReplanRequest(
        @Schema(
                description = "Updated upstream requirement used to create a new plan version",
                example = "Add URL expiration and preserve analytics"
        )
        @NotBlank String requirement) {
}
