package com.example.agenticurl.url.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record CreateUrlResponse(
        @Schema(example = "4816186")
        String shortCode,

        @Schema(example = "http://localhost:8080/4816186")
        String shortUrl,

        @Schema(
                description = "Expiration timestamp, or null when the URL does not expire",
                nullable = true
        )
        Instant expiresAt) {
}
