package com.example.agenticurl.url.dto;

import java.time.Instant;

public record UrlAnalytics(
        String shortCode,
        long clickCount,
        Instant createdAt,
        Instant expiresAt) {
}
