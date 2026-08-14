package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.IntentAnalysis;
import com.example.agenticurl.agent.enums.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntentAnalyzer {

    public IntentAnalysis analyze(
            String requirement,
            String clarification) {

        String lower = requirement.toLowerCase();

        if ((lower.contains("secure") || lower.contains("better"))
                && (clarification == null || clarification.isBlank())) {

            return new IntentAnalysis(
                    true,
                    null,
                    List.of(
                            "Which security outcome is required: rate limiting, authentication, "
                                    + "destination validation, or another control?"
                    ),
                    RiskLevel.HIGH
            );
        }

        String normalized = clarification == null || clarification.isBlank()
                ? requirement.trim()
                : requirement.trim() + ". Clarification: " + clarification.trim();

        String normalizedLower = normalized.toLowerCase();

        RiskLevel risk = normalizedLower.contains("schema")
                || normalizedLower.contains("expiration")
                || normalizedLower.contains("authentication")
                || normalizedLower.contains("authorization")
                ? RiskLevel.HIGH
                : normalizedLower.contains("api")
                || normalizedLower.contains("rate limit")
                ? RiskLevel.MEDIUM
                : RiskLevel.LOW;

        return new IntentAnalysis(
                false,
                normalized,
                List.of(),
                risk
        );
    }
}
