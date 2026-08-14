package com.example.agenticurl.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record DemoWorkflowRequest(
        @NotBlank String requirement,
        @Valid FaultInjectionRequest faults) {
}
