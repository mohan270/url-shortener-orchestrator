package com.example.agenticurl.agent.domain;

/**
 * Demo/test-only fault injection settings. Normal workflow requests always use none().
 */
public record FaultInjectionOptions(
        int buildFailuresBeforePass,
        boolean securityScannerUnavailable,
        boolean deliveryFailure) {

    public static FaultInjectionOptions none() {
        return new FaultInjectionOptions(0, false, false);
    }
}
