package com.example.agenticurl.agent.domain;

import java.util.Map;

public record StepResult(
        boolean success,
        String summary,
        boolean fallbackUsed,
        Map<String, Object> data) {

    public static StepResult ok(String summary, Map<String, Object> data) {
        return new StepResult(true, summary, false, data);
    }

    public static StepResult fallback(String summary, Map<String, Object> data) {
        return new StepResult(true, summary, true, data);
    }

    public static StepResult failed(String summary, Map<String, Object> data) {
        return new StepResult(false, summary, false, data);
    }
}
