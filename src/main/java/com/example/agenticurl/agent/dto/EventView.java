package com.example.agenticurl.agent.dto;

import com.example.agenticurl.agent.entity.ExecutionEventEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record EventView(
        int planVersion,
        String stepId,

        @Schema(example = "REVIEW_REQUESTED")
        String eventType,

        String details,
        Instant createdAt) {

    public static EventView from(ExecutionEventEntity event) {
        return new EventView(
                event.getPlanVersion(),
                event.getStepId(),
                event.getEventType(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
