package com.example.agenticurl.url.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequest(
        @Schema(
                description = "Destination URL to shorten",
                example = "https://www.google.com"
        )
        @NotBlank String url,

        @Schema(
                description = "Optional lifetime in days; omit for no expiration",
                example = "7"
        )
        Integer expiresInDays,

        @Schema(
                description = "Optional caller-supplied short code",
                example = "google-docs"
        )
        String customAlias) {
}
