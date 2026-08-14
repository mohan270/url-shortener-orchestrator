package com.example.agenticurl.url.service;

import com.example.agenticurl.url.config.UrlShortenerProperties;
import com.example.agenticurl.url.dto.CreatedShortUrl;
import com.example.agenticurl.url.entity.ShortUrlEntity;
import com.example.agenticurl.url.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class CreateShortUrlService {

    private static final int MAX_CODE_ALLOCATION_ATTEMPTS = 5;

    private final ShortUrlRepository repository;
    private final UrlShortenerProperties properties;

    public CreateShortUrlService(
            ShortUrlRepository repository,
            UrlShortenerProperties properties) {

        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public CreatedShortUrl create(
            String url,
            Integer expiresInDays,
            String customAlias) {

        validateUrl(url);
        validateExpiration(expiresInDays);

        String shortCode = customAlias == null || customAlias.isBlank()
                ? allocateCode()
                : customAlias.trim();

        if (repository.existsByShortCode(shortCode)) {
            throw new IllegalArgumentException("Alias already exists");
        }

        Instant expiresAt = expiresInDays == null
                ? null
                : Instant.now().plus(expiresInDays, ChronoUnit.DAYS);

        ShortUrlEntity saved = repository.save(
                new ShortUrlEntity(
                        shortCode,
                        url,
                        expiresAt
                )
        );

        return new CreatedShortUrl(
                saved.getShortCode(),
                properties.shortUrl(saved.getShortCode()),
                saved.getExpiresAt()
        );
    }

    private void validateUrl(String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("URL is not valid", exception);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http")
                || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only http/https URLs are supported");
        }
    }

    private void validateExpiration(Integer expiresInDays) {
        if (expiresInDays != null && expiresInDays <= 0) {
            throw new IllegalArgumentException("expiresInDays must be greater than zero");
        }
    }

    private String allocateCode() {
        for (int attempt = 0; attempt < MAX_CODE_ALLOCATION_ATTEMPTS; attempt++) {
            String candidate = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 7);

            if (!repository.existsByShortCode(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to allocate unique short code");
    }
}
