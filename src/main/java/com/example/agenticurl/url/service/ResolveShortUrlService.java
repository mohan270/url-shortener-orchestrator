package com.example.agenticurl.url.service;

import com.example.agenticurl.url.entity.ShortUrlEntity;
import com.example.agenticurl.url.exception.ExpiredUrlException;
import com.example.agenticurl.url.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.NoSuchElementException;

@Service
public class ResolveShortUrlService {

    private final ShortUrlRepository repository;

    public ResolveShortUrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public URI resolve(String shortCode) {
        ShortUrlEntity shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Unknown short code"));

        if (shortUrl.expired()) {
            throw new ExpiredUrlException(shortCode);
        }

        shortUrl.incrementClicks();
        return URI.create(shortUrl.getOriginalUrl());
    }
}
