package com.example.agenticurl.url.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UrlAnalyticsResponse(
        @Schema(example = "4816186")
        String shortCode,

        @Schema(example = "3")
        long clickCount,

        Instant createdAt,

        @Schema(nullable = true)
        Instant expiresAt) {
}
