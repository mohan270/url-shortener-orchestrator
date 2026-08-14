package com.example.agenticurl.agent.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_event")
public class ExecutionEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID workflowId;

    private int planVersion;
    private String stepId;

    @Column(nullable = false)
    private String eventType;

    @Lob
    private String details;

    private Instant createdAt;

    protected ExecutionEventEntity() {
    }

    public ExecutionEventEntity(
            UUID workflowId,
            int planVersion,
            String stepId,
            String eventType,
            String details) {

        this.workflowId = workflowId;
        this.planVersion = planVersion;
        this.stepId = stepId;
        this.eventType = eventType;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getWorkflowId() { return workflowId; }
    public int getPlanVersion() { return planVersion; }
    public String getStepId() { return stepId; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
