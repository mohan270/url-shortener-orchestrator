package com.example.agenticurl.url.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "short_url")
public class ShortUrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    private Instant createdAt;
    private Instant expiresAt;
    private long clickCount;

    protected ShortUrlEntity() {
    }

    public ShortUrlEntity(
            String shortCode,
            String originalUrl,
            Instant expiresAt) {

        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public long getClickCount() { return clickCount; }

    public boolean expired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void incrementClicks() {
        clickCount++;
    }
}
