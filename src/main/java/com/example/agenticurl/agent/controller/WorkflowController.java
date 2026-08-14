package com.example.agenticurl.agent.controller;

import com.example.agenticurl.agent.dto.*;
import com.example.agenticurl.agent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@Tag(
        name = "Engineering Workflows",
        description = "Create, review, inspect, clarify, and replan governed engineering workflows."
)
public class WorkflowController {

    private final StartWorkflowService startWorkflowService;
    private final ClarifyWorkflowService clarifyWorkflowService;
    private final ReviewWorkflowStepService reviewWorkflowStepService;
    private final ReplanWorkflowService replanWorkflowService;
    private final GetWorkflowService getWorkflowService;
    private final GetWorkflowEventsService getWorkflowEventsService;
    private final GetWorkflowStepsService getWorkflowStepsService;
    private final GetWorkflowApprovalsService getWorkflowApprovalsService;
    private final GetWorkflowMetricsService getWorkflowMetricsService;

    public WorkflowController(
            StartWorkflowService startWorkflowService,
            ClarifyWorkflowService clarifyWorkflowService,
            ReviewWorkflowStepService reviewWorkflowStepService,
            ReplanWorkflowService replanWorkflowService,
            GetWorkflowService getWorkflowService,
            GetWorkflowEventsService getWorkflowEventsService,
            GetWorkflowStepsService getWorkflowStepsService,
            GetWorkflowApprovalsService getWorkflowApprovalsService,
            GetWorkflowMetricsService getWorkflowMetricsService) {

        this.startWorkflowService = startWorkflowService;
        this.clarifyWorkflowService = clarifyWorkflowService;
        this.reviewWorkflowStepService = reviewWorkflowStepService;
        this.replanWorkflowService = replanWorkflowService;
        this.getWorkflowService = getWorkflowService;
        this.getWorkflowEventsService = getWorkflowEventsService;
        this.getWorkflowStepsService = getWorkflowStepsService;
        this.getWorkflowApprovalsService = getWorkflowApprovalsService;
        this.getWorkflowMetricsService = getWorkflowMetricsService;
    }

    @Operation(
            summary = "Start an engineering workflow",
            description = "Normalizes the requirement, creates a versioned dependency graph, persists it, and executes until completion or a governed pause."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workflow created and advanced to its current state"),
            @ApiResponse(responseCode = "400", description = "Invalid requirement")
    })
    @PostMapping
    public WorkflowView start(
            @Valid @RequestBody StartWorkflowRequest request) {

        return WorkflowView.from(
                startWorkflowService.start(request.requirement())
        );
    }

    @Operation(
            summary = "Provide clarification",
            description = "Supplies human clarification for an ambiguous requirement and resumes planning."
    )
    @PostMapping("/{id}/clarification")
    public WorkflowView clarify(
            @PathVariable UUID id,
            @Valid @RequestBody ClarificationRequest request) {

        return WorkflowView.from(
                clarifyWorkflowService.clarify(
                        id,
                        request.clarification()
                )
        );
    }

    @Operation(
            summary = "Review a workflow step",
            description = "Persists a human approval or rejection for a governed step and resumes execution when approved."
    )
    @PostMapping("/{id}/steps/{stepId}/review")
    public WorkflowView review(
            @PathVariable UUID id,
            @PathVariable String stepId,
            @Valid @RequestBody ReviewRequest request) {

        return WorkflowView.from(
                reviewWorkflowStepService.review(
                        id,
                        stepId,
                        request.decision(),
                        request.comment()
                )
        );
    }

    @Operation(
            summary = "Replan a workflow",
            description = "Replaces the upstream requirement, supersedes the current plan, creates a new plan version, and resumes governed execution."
    )
    @PostMapping("/{id}/replan")
    public WorkflowView replan(
            @PathVariable UUID id,
            @Valid @RequestBody ReplanRequest request) {

        return WorkflowView.from(
                replanWorkflowService.replan(
                        id,
                        request.requirement()
                )
        );
    }

    @Operation(summary = "Get workflow state")
    @GetMapping("/{id}")
    public WorkflowView get(@PathVariable UUID id) {
        return WorkflowView.from(getWorkflowService.get(id));
    }

    @Operation(
            summary = "Get workflow event history",
            description = "Returns the audit trail for planning, execution, reviews, replanning, recovery, and completion."
    )
    @GetMapping("/{id}/events")
    public List<EventView> events(@PathVariable UUID id) {
        return getWorkflowEventsService.get(id).stream()
                .map(EventView::from)
                .toList();
    }

    @Operation(
            summary = "Get execution graph nodes",
            description = "Returns persisted nodes across plan versions, including dependencies, policy, status, attempts, results, and superseded state."
    )
    @GetMapping("/{id}/steps")
    public List<StepView> steps(@PathVariable UUID id) {
        return getWorkflowStepsService.get(id).stream()
                .map(StepView::from)
                .toList();
    }

    @Operation(
            summary = "Get workflow reliability metrics",
            description = "Returns success rate, retry count, compensation/rollback count, MTTR, and end-to-end latency from persisted workflow state."
    )
    @GetMapping("/{id}/metrics")
    public WorkflowMetricsView metrics(@PathVariable UUID id) {
        return getWorkflowMetricsService.get(id);
    }

    @Operation(
            summary = "Get human approvals",
            description = "Returns durable approval decisions for workflow steps."
    )
    @GetMapping("/{id}/approvals")
    public List<ApprovalView> approvals(@PathVariable UUID id) {
        return getWorkflowApprovalsService.get(id).stream()
                .map(ApprovalView::from)
                .toList();
    }
}
