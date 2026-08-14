package com.example.agenticurl.agent.entity;

import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import com.example.agenticurl.agent.enums.RiskLevel;
import com.example.agenticurl.agent.enums.WorkflowStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "engineering_workflow")
public class WorkflowEntity {

    @Id
    private UUID id;

    @Lob
    @Column(nullable = false)
    private String requirement;

    @Lob
    private String clarification;

    @Lob
    private String normalizedRequirement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private int planVersion;
    private int automaticReplanCount;
    private String waitingStepId;

    private int buildFailuresBeforePass;
    private boolean securityScannerUnavailable;
    private boolean deliveryFailure;

    private Instant createdAt;
    private Instant updatedAt;

    protected WorkflowEntity() {
    }

    public WorkflowEntity(
            UUID id,
            String requirement,
            FaultInjectionOptions faultInjection) {

        this.id = id;
        this.requirement = requirement;
        this.status = WorkflowStatus.RECEIVED;
        this.planVersion = 1;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;

        this.buildFailuresBeforePass = faultInjection.buildFailuresBeforePass();
        this.securityScannerUnavailable = faultInjection.securityScannerUnavailable();
        this.deliveryFailure = faultInjection.deliveryFailure();
    }

    public UUID getId() { return id; }
    public String getRequirement() { return requirement; }
    public String getClarification() { return clarification; }
    public String getNormalizedRequirement() { return normalizedRequirement; }
    public WorkflowStatus getStatus() { return status; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public int getPlanVersion() { return planVersion; }
    public int getAutomaticReplanCount() { return automaticReplanCount; }
    public String getWaitingStepId() { return waitingStepId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setClarification(String clarification) {
        this.clarification = clarification;
        touch();
    }

    public void setNormalizedRequirement(String normalizedRequirement) {
        this.normalizedRequirement = normalizedRequirement;
        touch();
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
        touch();
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
        touch();
    }

    public void incrementPlanVersion() {
        this.planVersion++;
        touch();
    }

    public void incrementAutomaticReplanCount() {
        this.automaticReplanCount++;
        touch();
    }

    public void resetAutomaticReplanCount() {
        this.automaticReplanCount = 0;
        touch();
    }

    public void setWaitingStepId(String waitingStepId) {
        this.waitingStepId = waitingStepId;
        touch();
    }

    public void updateRequirement(String requirement) {
        this.requirement = requirement;
        this.clarification = null;
        this.normalizedRequirement = null;
        this.waitingStepId = null;
        touch();
    }

    public FaultInjectionOptions faultInjection() {
        return new FaultInjectionOptions(
                buildFailuresBeforePass,
                securityScannerUnavailable,
                deliveryFailure
        );
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
