package com.example.agenticurl.agent.domain;

import com.example.agenticurl.agent.enums.RiskLevel;

import java.util.List;

public record IntentAnalysis(
        boolean ambiguous,
        String normalizedRequirement,
        List<String> questions,
        RiskLevel riskLevel) {
}
