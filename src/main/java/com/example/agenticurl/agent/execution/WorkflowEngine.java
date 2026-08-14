package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.*;
import com.example.agenticurl.agent.entity.*;
import com.example.agenticurl.agent.enums.*;
import com.example.agenticurl.agent.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowEngine {

    private static final int MAX_AUTOMATIC_REPLANS = 1;

    private final WorkflowRepository workflowRepository;
    private final ExecutionEventRepository eventRepository;
    private final ExecutionStepRepository stepRepository;
    private final ApprovalRepository approvalRepository;
    private final IntentAnalyzer intentAnalyzer;
    private final PlanBuilder planBuilder;
    private final GraphScheduler scheduler;
    private final ObjectMapper objectMapper;

    public WorkflowEngine(
            WorkflowRepository workflowRepository,
            ExecutionEventRepository eventRepository,
            ExecutionStepRepository stepRepository,
            ApprovalRepository approvalRepository,
            IntentAnalyzer intentAnalyzer,
            PlanBuilder planBuilder,
            GraphScheduler scheduler,
            ObjectMapper objectMapper) {

        this.workflowRepository = workflowRepository;
        this.eventRepository = eventRepository;
        this.stepRepository = stepRepository;
        this.approvalRepository = approvalRepository;
        this.intentAnalyzer = intentAnalyzer;
        this.planBuilder = planBuilder;
        this.scheduler = scheduler;
        this.objectMapper = objectMapper;
    }

    public WorkflowEntity start(
            String requirement,
            FaultInjectionOptions faultInjection) {

        WorkflowEntity workflow = new WorkflowEntity(
                UUID.randomUUID(),
                requirement,
                faultInjection == null ? FaultInjectionOptions.none() : faultInjection
        );

        workflowRepository.save(workflow);
        record(workflow, null, "REQUEST_ACCEPTED", requirement);

        return analyzeAndRun(workflow);
    }

    public WorkflowEntity clarify(
            UUID id,
            String clarification) {

        WorkflowEntity workflow = getWorkflow(id);

        workflow.setClarification(clarification);
        workflow.setStatus(WorkflowStatus.RECEIVED);

        record(workflow, null, "CLARIFICATION_PROVIDED", clarification);

        return analyzeAndRun(workflow);
    }

    public WorkflowEntity review(
            UUID workflowId,
            String stepId,
            ReviewDecision decision,
            String comment) {

        WorkflowEntity workflow = getWorkflow(workflowId);

        ExecutionStepEntity step = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflowId,
                        workflow.getPlanVersion(),
                        stepId
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown step " + stepId
                ));

        if (step.getStatus() != StepStatus.WAITING_REVIEW) {
            throw new IllegalStateException(
                    "Step " + stepId + " is not waiting for review"
            );
        }

        ApprovalPhase phase = step.getExecutionMode() == ExecutionMode.REVIEW_AFTER_EXECUTION
                ? ApprovalPhase.AFTER_EXECUTION
                : ApprovalPhase.BEFORE_EXECUTION;

        if (approvalRepository
                .existsByWorkflowIdAndPlanVersionAndStepIdAndPhase(
                        workflowId,
                        workflow.getPlanVersion(),
                        stepId,
                        phase
                )) {
            throw new IllegalStateException(
                    "A review decision already exists for step " + stepId
            );
        }

        approvalRepository.save(new ApprovalEntity(
                workflowId,
                workflow.getPlanVersion(),
                stepId,
                phase,
                decision,
                comment
        ));

        if (decision == ReviewDecision.REJECTED) {
            workflow.setStatus(WorkflowStatus.REJECTED);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    stepId,
                    "REVIEW_REJECTED",
                    comment == null ? "rejected" : comment
            );

            return workflowRepository.save(workflow);
        }

        workflow.setWaitingStepId(null);
        workflow.setStatus(WorkflowStatus.RUNNING);

        record(
                workflow,
                stepId,
                "REVIEW_APPROVED",
                comment == null ? "approved" : comment
        );

        return runPlan(workflow);
    }

    public WorkflowEntity replan(
            UUID id,
            String newRequirement) {

        WorkflowEntity workflow = getWorkflow(id);
        int previousPlanVersion = workflow.getPlanVersion();

        supersedePlan(
                workflow.getId(),
                previousPlanVersion
        );

        workflow.incrementPlanVersion();
        workflow.resetAutomaticReplanCount();
        workflow.updateRequirement(newRequirement);
        workflow.setStatus(WorkflowStatus.REPLANNING);

        record(
                workflow,
                null,
                "PLAN_INVALIDATED",
                "Upstream requirement changed; previousPlanVersion="
                        + previousPlanVersion
                        + ", newPlanVersion="
                        + workflow.getPlanVersion()
        );

        return analyzeAndRun(workflow);
    }

    private WorkflowEntity analyzeAndRun(
            WorkflowEntity workflow) {

        IntentAnalysis analysis = intentAnalyzer.analyze(
                workflow.getRequirement(),
                workflow.getClarification()
        );

        workflow.setRiskLevel(analysis.riskLevel());

        if (analysis.ambiguous()) {
            workflow.setStatus(WorkflowStatus.NEEDS_CLARIFICATION);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    null,
                    "AMBIGUITY_DETECTED",
                    String.join(" | ", analysis.questions())
            );

            return workflowRepository.save(workflow);
        }

        workflow.setNormalizedRequirement(
                analysis.normalizedRequirement()
        );
        workflow.setStatus(WorkflowStatus.PLANNED);

        ExecutionPlan plan = planBuilder.build(
                workflow.getPlanVersion(),
                analysis
        );

        persistPlan(workflow, plan);

        record(
                workflow,
                null,
                "PLAN_CREATED",
                "planVersion=" + workflow.getPlanVersion()
                        + ", stepCount=" + plan.steps().size()
        );

        return runPlan(workflow);
    }

    private WorkflowEntity runPlan(
            WorkflowEntity workflow) {

        workflow.setStatus(WorkflowStatus.RUNNING);
        workflowRepository.save(workflow);

        Map<String, Object> sharedState = new ConcurrentHashMap<>(
                rehydrateSharedState(workflow)
        );

        ExecutionContext context = new ExecutionContext(
                workflow.getId(),
                workflow.getNormalizedRequirement(),
                workflow.getClarification(),
                workflow.getPlanVersion(),
                workflow.getRiskLevel(),
                workflow.faultInjection(),
                sharedState
        );

        GraphScheduler.SchedulerResult result = scheduler.run(
                context,
                (step, stepResult) -> record(
                        workflow,
                        step.getStepId(),
                        stepResult.success()
                                ? "STEP_EXECUTION_SUCCEEDED"
                                : "STEP_EXECUTION_FAILED",
                        stepResult.summary()
                                + (stepResult.fallbackUsed()
                                ? " fallbackUsed=true"
                                : "")
                )
        );

        if (result.waitingStepId() != null) {
            workflow.setWaitingStepId(result.waitingStepId());
            workflow.setStatus(WorkflowStatus.WAITING_REVIEW);

            record(
                    workflow,
                    result.waitingStepId(),
                    "REVIEW_REQUESTED",
                    "Human review required by step execution policy"
            );

        } else if (result.compensationRequired()) {
            workflow.setStatus(WorkflowStatus.COMPENSATED);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    result.failedStepId(),
                    "COMPENSATION_COMPLETED",
                    "Proposed engineering change discarded and not released"
            );

        } else if (result.replanningRecommended()) {
            return handleAutomaticReplan(
                    workflow,
                    result.failedStepId()
            );

        } else if (result.completed()) {
            workflow.setStatus(WorkflowStatus.COMPLETED);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    null,
                    "WORKFLOW_COMPLETED",
                    "All execution nodes completed"
            );

        } else {
            workflow.setStatus(WorkflowStatus.SAFE_STOP);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    result.failedStepId(),
                    "SAFE_STOP",
                    "No safe continuation exists"
            );
        }

        return workflowRepository.save(workflow);
    }

    private WorkflowEntity handleAutomaticReplan(
            WorkflowEntity workflow,
            String failedStepId) {

        if (workflow.getAutomaticReplanCount() >= MAX_AUTOMATIC_REPLANS) {
            workflow.setStatus(WorkflowStatus.SAFE_STOP);
            workflow.setWaitingStepId(null);

            record(
                    workflow,
                    failedStepId,
                    "SAFE_STOP",
                    "Automatic replanning budget exhausted"
            );

            return workflowRepository.save(workflow);
        }

        int previousPlanVersion = workflow.getPlanVersion();

        supersedePlan(
                workflow.getId(),
                previousPlanVersion
        );

        workflow.incrementAutomaticReplanCount();
        workflow.incrementPlanVersion();
        workflow.setWaitingStepId(null);
        workflow.setStatus(WorkflowStatus.REPLANNING);

        record(
                workflow,
                failedStepId,
                "AUTOMATIC_REPLAN_TRIGGERED",
                "Failure policy requested a new plan; previousPlanVersion="
                        + previousPlanVersion
                        + ", newPlanVersion="
                        + workflow.getPlanVersion()
        );

        return analyzeAndRun(workflow);
    }

    private void persistPlan(
            WorkflowEntity workflow,
            ExecutionPlan plan) {

        if (stepRepository.existsByWorkflowIdAndPlanVersion(
                workflow.getId(),
                workflow.getPlanVersion())) {
            return;
        }

        List<ExecutionStepEntity> entities = plan.steps().stream()
                .map(step -> new ExecutionStepEntity(
                        workflow.getId(),
                        workflow.getPlanVersion(),
                        step
                ))
                .toList();

        stepRepository.saveAll(entities);
    }

    private void supersedePlan(
            UUID workflowId,
            int planVersion) {

        List<ExecutionStepEntity> current = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflowId,
                        planVersion
                );

        current.forEach(ExecutionStepEntity::markSuperseded);
        stepRepository.saveAll(current);
    }

    private Map<String, Object> rehydrateSharedState(
            WorkflowEntity workflow) {

        Map<String, Object> sharedState = new HashMap<>();

        List<ExecutionStepEntity> steps = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflow.getId(),
                        workflow.getPlanVersion()
                );

        for (ExecutionStepEntity step : steps) {
            if (step.getStatus() != StepStatus.SUCCEEDED
                    || step.getResultDataJson() == null
                    || step.getResultDataJson().isBlank()) {
                continue;
            }

            try {
                Map<String, Object> data = objectMapper.readValue(
                        step.getResultDataJson(),
                        new TypeReference<>() {
                        }
                );

                sharedState.put(step.getStepId(), data);

            } catch (Exception exception) {
                throw new IllegalStateException(
                        "Could not rehydrate context for step "
                                + step.getStepId(),
                        exception
                );
            }
        }

        return sharedState;
    }

    private WorkflowEntity getWorkflow(UUID id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Workflow not found"
                ));
    }

    private void record(
            WorkflowEntity workflow,
            String stepId,
            String eventType,
            String details) {

        eventRepository.save(new ExecutionEventEntity(
                workflow.getId(),
                workflow.getPlanVersion(),
                stepId,
                eventType,
                details
        ));
    }
}
