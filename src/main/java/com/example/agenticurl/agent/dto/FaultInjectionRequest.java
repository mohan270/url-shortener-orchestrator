package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import jakarta.validation.constraints.Min;

public record FaultInjectionRequest(
        @Min(0) int buildFailuresBeforePass,
        boolean securityScannerUnavailable,
        boolean deliveryFailure) {

    public FaultInjectionOptions toOptions() {
        return new FaultInjectionOptions(
                buildFailuresBeforePass,
                securityScannerUnavailable,
                deliveryFailure
        );
    }
}
