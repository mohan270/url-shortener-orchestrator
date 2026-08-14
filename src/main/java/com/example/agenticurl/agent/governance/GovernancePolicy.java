package com.example.agenticurl.agent.governance;

import com.example.agenticurl.agent.enums.ExecutionMode;
import com.example.agenticurl.agent.enums.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class GovernancePolicy {

    public ExecutionMode apiDesignMode(RiskLevel riskLevel) {
        return riskLevel == RiskLevel.LOW
                ? ExecutionMode.AUTO
                : ExecutionMode.REVIEW_BEFORE_EXECUTION;
    }

    public ExecutionMode dataDesignMode() {
        return ExecutionMode.REVIEW_BEFORE_EXECUTION;
    }

    public ExecutionMode deliveryMode(RiskLevel riskLevel) {
        return riskLevel == RiskLevel.HIGH
                ? ExecutionMode.REVIEW_AFTER_EXECUTION
                : ExecutionMode.AUTO;
    }
}
