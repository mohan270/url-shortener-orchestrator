package com.example.agenticurl.agent;

import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.enums.*;
import com.example.agenticurl.agent.repository.ApprovalRepository;
import com.example.agenticurl.agent.repository.ExecutionStepRepository;
import com.example.agenticurl.agent.service.ReplanWorkflowService;
import com.example.agenticurl.agent.service.ReviewWorkflowStepService;
import com.example.agenticurl.agent.service.StartWorkflowService;
import com.example.agenticurl.agent.service.StartDemoWorkflowService;
import com.example.agenticurl.agent.service.GetWorkflowMetricsService;
import com.example.agenticurl.agent.dto.WorkflowMetricsView;
import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflowtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EngineeringWorkflowIntegrationTest {

    @Autowired
    StartWorkflowService startWorkflowService;

    @Autowired
    StartDemoWorkflowService startDemoWorkflowService;

    @Autowired
    ReviewWorkflowStepService reviewWorkflowStepService;

    @Autowired
    ReplanWorkflowService replanWorkflowService;

    @Autowired
    ExecutionStepRepository stepRepository;

    @Autowired
    ApprovalRepository approvalRepository;

    @Autowired
    GetWorkflowMetricsService getWorkflowMetricsService;

    @Test
    void ambiguousRequirementStopsBeforePlanning() {
        WorkflowEntity workflow = startWorkflowService.start(
                "Make the URL shortener more secure"
        );

        assertEquals(
                WorkflowStatus.NEEDS_CLARIFICATION,
                workflow.getStatus()
        );

        assertTrue(
                stepRepository
                        .findByWorkflowIdOrderByPlanVersionAscIdAsc(workflow.getId())
                        .isEmpty()
        );
    }

    @Test
    void executionNodesArePersisted() {
        WorkflowEntity workflow = startWorkflowService.start(
                "Add expiration support to short URLs"
        );

        List<ExecutionStepEntity> steps = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflow.getId(),
                        1
                );

        assertFalse(steps.isEmpty());

        assertTrue(steps.stream().anyMatch(step ->
                step.getStepId().equals("impact")
                        && step.getStatus() == StepStatus.SUCCEEDED
        ));

        assertTrue(steps.stream().anyMatch(step ->
                step.getStatus() == StepStatus.WAITING_REVIEW
        ));
    }

    @Test
    void approvalIsPersistedAndWorkflowResumes() {
        WorkflowEntity workflow = startWorkflowService.start(
                "Add expiration support to short URLs"
        );

        String waitingStep = workflow.getWaitingStepId();
        assertNotNull(waitingStep);

        WorkflowEntity resumed = reviewWorkflowStepService.review(
                workflow.getId(),
                waitingStep,
                ReviewDecision.APPROVED,
                "Reviewed by developer"
        );

        assertTrue(
                approvalRepository
                        .existsByWorkflowIdAndPlanVersionAndStepIdAndPhaseAndDecision(
                                workflow.getId(),
                                1,
                                waitingStep,
                                ApprovalPhase.BEFORE_EXECUTION,
                                ReviewDecision.APPROVED
                        )
        );

        assertEquals(
                WorkflowStatus.WAITING_REVIEW,
                resumed.getStatus()
        );
        assertNotEquals(waitingStep, resumed.getWaitingStepId());
    }

    @Test
    void brownfieldFlowDemonstratesRetryFallbackAndFinalReview() {
        WorkflowEntity workflow = startDemoWorkflowService.start(
                "Add expiration support to short URLs",
                new FaultInjectionOptions(1, true, false)
        );

        // Approve the two REVIEW_BEFORE nodes: api-design and data-design.
        workflow = reviewWorkflowStepService.review(
                workflow.getId(),
                workflow.getWaitingStepId(),
                ReviewDecision.APPROVED,
                "API design approved"
        );

        workflow = reviewWorkflowStepService.review(
                workflow.getId(),
                workflow.getWaitingStepId(),
                ReviewDecision.APPROVED,
                "Data design approved"
        );

        assertEquals(WorkflowStatus.WAITING_REVIEW, workflow.getStatus());
        assertEquals("delivery", workflow.getWaitingStepId());

        ExecutionStepEntity build = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflow.getId(),
                        1,
                        "build"
                )
                .orElseThrow();

        ExecutionStepEntity security = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflow.getId(),
                        1,
                        "security"
                )
                .orElseThrow();

        ExecutionStepEntity deliveryBeforeApproval = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflow.getId(),
                        1,
                        "delivery"
                )
                .orElseThrow();

        assertEquals(2, build.getAttemptCount());
        assertTrue(security.isFallbackUsed());
        assertEquals(1, deliveryBeforeApproval.getAttemptCount());
        assertEquals(StepStatus.WAITING_REVIEW, deliveryBeforeApproval.getStatus());

        workflow = reviewWorkflowStepService.review(
                workflow.getId(),
                "delivery",
                ReviewDecision.APPROVED,
                "Final outcome approved"
        );

        assertEquals(WorkflowStatus.COMPLETED, workflow.getStatus());

        ExecutionStepEntity deliveryAfterApproval = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflow.getId(),
                        1,
                        "delivery"
                )
                .orElseThrow();

        // REVIEW_AFTER must not execute the step a second time.
        assertEquals(1, deliveryAfterApproval.getAttemptCount());
        assertEquals(StepStatus.SUCCEEDED, deliveryAfterApproval.getStatus());

        WorkflowMetricsView metrics = getWorkflowMetricsService.get(workflow.getId());
        assertEquals(100.0, metrics.successRatePercent());
        assertEquals(1, metrics.retryCount());
        assertEquals(0, metrics.rollbackCount());
        assertTrue(metrics.endToEndLatencyMs() >= 0);
    }

    @Test
    void changedRequirementCreatesNewPlanVersionAndPreservesOldPlan() {
        WorkflowEntity workflow = startWorkflowService.start(
                "Add analytics API"
        );

        WorkflowEntity replanned = replanWorkflowService.replan(
                workflow.getId(),
                "Add expiration support to short URLs"
        );

        assertEquals(2, replanned.getPlanVersion());

        List<ExecutionStepEntity> oldPlan = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflow.getId(),
                        1
                );

        assertTrue(oldPlan.stream().allMatch(ExecutionStepEntity::isSuperseded));

        List<ExecutionStepEntity> newPlan = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflow.getId(),
                        2
                );

        assertTrue(newPlan.stream().anyMatch(step ->
                step.getStepId().equals("data-design")
        ));
    }

    @Test
    void retryExhaustionTriggersOneAutomaticReplanThenSafeStop() {
        WorkflowEntity workflow = startDemoWorkflowService.start(
                "Improve URL shortener documentation",
                new FaultInjectionOptions(99, false, false)
        );

        assertEquals(2, workflow.getPlanVersion());
        assertEquals(1, workflow.getAutomaticReplanCount());
        assertEquals(WorkflowStatus.SAFE_STOP, workflow.getStatus());

        List<ExecutionStepEntity> firstPlan = stepRepository
                .findByWorkflowIdAndPlanVersionOrderById(
                        workflow.getId(),
                        1
                );

        assertTrue(firstPlan.stream().allMatch(ExecutionStepEntity::isSuperseded));

        ExecutionStepEntity secondPlanBuild = stepRepository
                .findByWorkflowIdAndPlanVersionAndStepId(
                        workflow.getId(),
                        2,
                        "build"
                )
                .orElseThrow();

        assertEquals(3, secondPlanBuild.getAttemptCount());
        assertEquals(StepStatus.FAILED, secondPlanBuild.getStatus());
    }
}
