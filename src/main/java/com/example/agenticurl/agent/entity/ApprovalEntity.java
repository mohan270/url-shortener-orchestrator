package com.example.agenticurl.agent.entity;

import com.example.agenticurl.agent.enums.ApprovalPhase;
import com.example.agenticurl.agent.enums.ReviewDecision;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "execution_approval",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workflow_plan_step_phase",
                columnNames = {"workflow_id", "plan_version", "step_id", "phase"}
        )
)
public class ApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "plan_version", nullable = false)
    private int planVersion;

    @Column(name = "step_id", nullable = false)
    private String stepId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewDecision decision;

    @Lob
    private String comment;

    private Instant createdAt;

    protected ApprovalEntity() {
    }

    public ApprovalEntity(
            UUID workflowId,
            int planVersion,
            String stepId,
            ApprovalPhase phase,
            ReviewDecision decision,
            String comment) {

        this.workflowId = workflowId;
        this.planVersion = planVersion;
        this.stepId = stepId;
        this.phase = phase;
        this.decision = decision;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getWorkflowId() { return workflowId; }
    public int getPlanVersion() { return planVersion; }
    public String getStepId() { return stepId; }
    public ApprovalPhase getPhase() { return phase; }
    public ReviewDecision getDecision() { return decision; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
