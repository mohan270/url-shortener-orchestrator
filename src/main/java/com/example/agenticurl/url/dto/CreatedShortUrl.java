package com.example.agenticurl.url.dto;

import java.time.Instant;

public record CreatedShortUrl(
        String shortCode,
        String shortUrl,
        Instant expiresAt) {
}
