package com.example.agenticurl.url.controller;

import com.example.agenticurl.url.dto.CreateUrlRequest;
import com.example.agenticurl.url.dto.CreateUrlResponse;
import com.example.agenticurl.url.dto.CreatedShortUrl;
import com.example.agenticurl.url.dto.UrlAnalytics;
import com.example.agenticurl.url.dto.UrlAnalyticsResponse;
import com.example.agenticurl.url.exception.ExpiredUrlException;
import com.example.agenticurl.url.service.CreateShortUrlService;
import com.example.agenticurl.url.service.GetUrlAnalyticsService;
import com.example.agenticurl.url.service.ResolveShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@Tag(
        name = "URL Shortener",
        description = "Create short URLs, resolve redirects, and inspect click analytics."
)
public class UrlController {

    private final CreateShortUrlService createShortUrlService;
    private final ResolveShortUrlService resolveShortUrlService;
    private final GetUrlAnalyticsService getUrlAnalyticsService;

    public UrlController(
            CreateShortUrlService createShortUrlService,
            ResolveShortUrlService resolveShortUrlService,
            GetUrlAnalyticsService getUrlAnalyticsService) {

        this.createShortUrlService = createShortUrlService;
        this.resolveShortUrlService = resolveShortUrlService;
        this.getUrlAnalyticsService = getUrlAnalyticsService;
    }

    @Operation(
            summary = "Create a short URL",
            description = "Creates a short code for a destination URL with optional expiration and custom alias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Custom alias already exists")
    })
    @PostMapping("/api/urls")
    public ResponseEntity<CreateUrlResponse> create(
            @Valid @RequestBody CreateUrlRequest request) {

        CreatedShortUrl created = createShortUrlService.create(
                request.url(),
                request.expiresInDays(),
                request.customAlias()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateUrlResponse(
                        created.shortCode(),
                        created.shortUrl(),
                        created.expiresAt()
                ));
    }

    @Operation(
            summary = "Resolve a short URL",
            description = "Returns an HTTP 302 redirect to the original destination and records the click."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "410", description = "Short URL expired")
    })
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        URI destination = resolveShortUrlService.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(destination)
                .build();
    }

    @Operation(
            summary = "Get URL analytics",
            description = "Returns click count and lifecycle timestamps for a short URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics returned"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/api/urls/{code}/analytics")
    public UrlAnalyticsResponse analytics(@PathVariable String code) {
        UrlAnalytics analytics = getUrlAnalyticsService.get(code);

        return new UrlAnalyticsResponse(
                analytics.shortCode(),
                analytics.clickCount(),
                analytics.createdAt(),
                analytics.expiresAt()
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(
            NoSuchElementException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(ExpiredUrlException.class)
    public ResponseEntity<Map<String, String>> expired(
            ExpiredUrlException exception) {

        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(
            IllegalArgumentException exception) {

        HttpStatus status = exception.getMessage() != null
                && exception.getMessage().contains("Alias")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(Map.of("error", exception.getMessage()));
    }
}
