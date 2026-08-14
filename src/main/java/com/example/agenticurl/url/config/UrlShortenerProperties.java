package com.example.agenticurl.url.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.url-shortener")
public record UrlShortenerProperties(String baseUrl) {

    public String shortUrl(String code) {
        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalizedBase + "/" + code;
    }
}
