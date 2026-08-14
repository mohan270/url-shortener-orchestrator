package com.example.agenticurl.agent.domain;

import com.example.agenticurl.agent.enums.RiskLevel;

import java.util.Map;
import java.util.UUID;

public record ExecutionContext(
        UUID workflowId,
        String requirement,
        String clarification,
        int planVersion,
        RiskLevel riskLevel,
        FaultInjectionOptions faultInjection,
        Map<String, Object> sharedState) {
}
