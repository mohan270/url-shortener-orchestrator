package com.example.agenticurl.url.service;

import com.example.agenticurl.url.dto.UrlAnalytics;
import com.example.agenticurl.url.entity.ShortUrlEntity;
import com.example.agenticurl.url.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class GetUrlAnalyticsService {

    private final ShortUrlRepository repository;

    public GetUrlAnalyticsService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UrlAnalytics get(String shortCode) {
        ShortUrlEntity shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Unknown short code"));

        return new UrlAnalytics(
                shortUrl.getShortCode(),
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt()
        );
    }
}
