package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.enums.StepType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImplementationStep implements ExecutionStep {

    @Override
    public StepType type() {
        return StepType.IMPLEMENTATION;
    }

    @Override
    public StepResult execute(ExecutionContext context) {
        boolean expirationChange = context.requirement()
                .toLowerCase()
                .contains("expiration");

        List<String> files = expirationChange
                ? List.of(
                        "ShortUrlEntity.java",
                        "CreateUrlRequest.java",
                        "CreateShortUrlService.java",
                        "ResolveShortUrlService.java",
                        "UrlController.java"
                )
                : List.of(
                        "CreateShortUrlService.java",
                        "ResolveShortUrlService.java",
                        "UrlController.java"
                );

        List<String> proposedChanges = expirationChange
                ? List.of(
                        "Persist an expiresAt value for shortened URLs",
                        "Accept optional expiration input when a short URL is created",
                        "Reject redirects for expired links with HTTP 410",
                        "Keep analytics and existing redirect behavior intact"
                )
                : List.of(
                        "Implement the normalized requirement in the impacted URL-shortener components",
                        "Preserve existing API contracts unless the design step explicitly changes them"
                );

        return StepResult.ok(
                "Reviewable implementation change set generated",
                Map.of(
                        "files", files,
                        "changes", proposedChanges,
                        "sourceMutation", false
                )
        );
    }
}
