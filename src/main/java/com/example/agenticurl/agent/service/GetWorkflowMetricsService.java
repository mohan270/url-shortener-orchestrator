package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.dto.WorkflowMetricsView;
import com.example.agenticurl.agent.entity.ExecutionEventEntity;
import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.enums.StepStatus;
import com.example.agenticurl.agent.repository.ExecutionEventRepository;
import com.example.agenticurl.agent.repository.ExecutionStepRepository;
import com.example.agenticurl.agent.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetWorkflowMetricsService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionStepRepository stepRepository;
    private final ExecutionEventRepository eventRepository;

    public GetWorkflowMetricsService(
            WorkflowRepository workflowRepository,
            ExecutionStepRepository stepRepository,
            ExecutionEventRepository eventRepository) {

        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public WorkflowMetricsView get(UUID workflowId) {
        WorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NoSuchElementException("Workflow not found"));

        List<ExecutionStepEntity> steps = stepRepository
                .findByWorkflowIdOrderByPlanVersionAscIdAsc(workflowId);

        List<ExecutionEventEntity> events = eventRepository
                .findByWorkflowIdOrderById(workflowId);

        long succeeded = steps.stream()
                .filter(step -> step.getStatus() == StepStatus.SUCCEEDED)
                .count();

        long failed = steps.stream()
                .filter(step -> step.getStatus() == StepStatus.FAILED)
                .count();

        long terminal = succeeded + failed;
        double successRate = terminal == 0
                ? 0.0
                : Math.round((succeeded * 10000.0) / terminal) / 100.0;

        long retryCount = steps.stream()
                .mapToLong(step -> Math.max(0, step.getAttemptCount() - 1L))
                .sum();

        long rollbackCount = events.stream()
                .filter(event -> "COMPENSATION_COMPLETED".equals(event.getEventType()))
                .count();

        long meanTimeToRecoveryMs = Math.round(
                steps.stream()
                        .filter(step -> step.getStatus() == StepStatus.SUCCEEDED)
                        .filter(step -> step.getAttemptCount() > 1 || step.isFallbackUsed())
                        .filter(step -> step.getStartedAt() != null && step.getCompletedAt() != null)
                        .mapToLong(step -> Duration.between(
                                step.getStartedAt(),
                                step.getCompletedAt()
                        ).toMillis())
                        .average()
                        .orElse(0.0)
        );

        Instant end = workflow.getUpdatedAt() == null
                ? Instant.now()
                : workflow.getUpdatedAt();

        long endToEndLatencyMs = workflow.getCreatedAt() == null
                ? 0L
                : Math.max(0L, Duration.between(workflow.getCreatedAt(), end).toMillis());

        return new WorkflowMetricsView(
                successRate,
                retryCount,
                rollbackCount,
                meanTimeToRecoveryMs,
                endToEndLatencyMs
        );
    }
}
