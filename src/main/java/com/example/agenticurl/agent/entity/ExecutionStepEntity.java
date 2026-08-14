package com.example.agenticurl.agent.entity;

import com.example.agenticurl.agent.domain.ExecutionPolicy;
import com.example.agenticurl.agent.domain.StepDefinition;
import com.example.agenticurl.agent.enums.ExecutionMode;
import com.example.agenticurl.agent.enums.FailureStrategy;
import com.example.agenticurl.agent.enums.StepStatus;
import com.example.agenticurl.agent.enums.StepType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "execution_step",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workflow_plan_step",
                columnNames = {"workflow_id", "plan_version", "step_id"}
        )
)
public class ExecutionStepEntity {

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
    private StepType type;

    @Lob
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "execution_step_dependency",
            joinColumns = @JoinColumn(name = "execution_step_entity_id")
    )
    @Column(name = "dependency_step_id")
    private List<String> dependencies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionMode executionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FailureStrategy failureStrategy;

    private int maxRetries;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status;

    private int attemptCount;

    @Lob
    private String resultSummary;

    @Lob
    private String resultDataJson;

    private boolean fallbackUsed;
    private boolean superseded;

    private Instant startedAt;
    private Instant completedAt;
    private Instant updatedAt;

    protected ExecutionStepEntity() {
    }

    public ExecutionStepEntity(
            UUID workflowId,
            int planVersion,
            StepDefinition definition) {

        this.workflowId = workflowId;
        this.planVersion = planVersion;
        this.stepId = definition.id();
        this.type = definition.type();
        this.description = definition.description();
        this.dependencies = new ArrayList<>(definition.dependencies());
        this.executionMode = definition.policy().executionMode();
        this.failureStrategy = definition.policy().failureStrategy();
        this.maxRetries = definition.policy().maxRetries();
        this.status = StepStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    public void markPending() {
        this.status = StepStatus.PENDING;
        touch();
    }

    public void markWaitingReview() {
        this.status = StepStatus.WAITING_REVIEW;
        touch();
    }

    public void markRunning() {
        this.status = StepStatus.RUNNING;
        if (startedAt == null) {
            startedAt = Instant.now();
        }
        touch();
    }

    public void markSucceeded(
            int attemptsUsed,
            String summary,
            String resultDataJson,
            boolean fallbackUsed) {

        this.status = StepStatus.SUCCEEDED;
        this.attemptCount += attemptsUsed;
        this.resultSummary = summary;
        this.resultDataJson = resultDataJson;
        this.fallbackUsed = fallbackUsed;
        this.completedAt = Instant.now();
        touch();
    }

    public void markFailed(
            int attemptsUsed,
            String summary,
            String resultDataJson) {

        this.status = StepStatus.FAILED;
        this.attemptCount += attemptsUsed;
        this.resultSummary = summary;
        this.resultDataJson = resultDataJson;
        this.completedAt = Instant.now();
        touch();
    }

    public void approveCompletedStep() {
        this.status = StepStatus.SUCCEEDED;
        touch();
    }

    public void markSuperseded() {
        this.superseded = true;
        touch();
    }

    public StepDefinition toDefinition() {
        return new StepDefinition(
                stepId,
                type,
                description,
                List.copyOf(dependencies),
                new ExecutionPolicy(maxRetries, failureStrategy, executionMode)
        );
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getWorkflowId() { return workflowId; }
    public int getPlanVersion() { return planVersion; }
    public String getStepId() { return stepId; }
    public StepType getType() { return type; }
    public String getDescription() { return description; }
    public List<String> getDependencies() { return List.copyOf(dependencies); }
    public ExecutionMode getExecutionMode() { return executionMode; }
    public FailureStrategy getFailureStrategy() { return failureStrategy; }
    public int getMaxRetries() { return maxRetries; }
    public StepStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public String getResultSummary() { return resultSummary; }
    public String getResultDataJson() { return resultDataJson; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public boolean isSuperseded() { return superseded; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
