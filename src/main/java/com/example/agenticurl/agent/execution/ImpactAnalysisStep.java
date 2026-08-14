package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImpactAnalysisStep implements ExecutionStep {

    @Override
    public StepType type() {
        return StepType.IMPACT_ANALYSIS;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        String requirement = context.requirement().toLowerCase();
        boolean expirationChange = requirement.contains("expiration");

        List<String> impactedComponents = expirationChange
                ? List.of(
                        "url.entity.ShortUrlEntity",
                        "url.dto.CreateUrlRequest",
                        "url.service.CreateShortUrlService",
                        "url.service.ResolveShortUrlService",
                        "url.service.GetUrlAnalyticsService",
                        "url.controller.UrlController"
                )
                : List.of(
                        "url.service.CreateShortUrlService",
                        "url.service.ResolveShortUrlService",
                        "url.controller.UrlController"
                );

        List<String> impactedApis = expirationChange
                ? List.of(
                        "POST /api/urls",
                        "GET /{shortCode}",
                        "GET /api/urls/{shortCode}/analytics"
                )
                : List.of(
                        "POST /api/urls",
                        "GET /{shortCode}"
                );

        List<String> dataFlows = expirationChange
                ? List.of(
                        "Create request -> CreateShortUrlService -> ShortUrlRepository -> ShortUrlEntity",
                        "Redirect request -> ResolveShortUrlService -> ShortUrlRepository -> expiration check -> redirect/410",
                        "Analytics request -> GetUrlAnalyticsService -> ShortUrlRepository -> analytics response"
                )
                : List.of(
                        "Create request -> CreateShortUrlService -> ShortUrlRepository",
                        "Redirect request -> ResolveShortUrlService -> ShortUrlRepository -> redirect"
                );

        return StepResult.ok(
                "Brownfield impact analysis completed",
                Map.of(
                        "components", impactedComponents,
                        "apis", impactedApis,
                        "dataFlows", dataFlows
                )
        );
    }
}
