package com.example.agenticurl.agent.execution;

import com.example.agenticurl.agent.domain.ExecutionContext;
import com.example.agenticurl.agent.domain.StepDefinition;
import com.example.agenticurl.agent.domain.StepResult;
import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import com.example.agenticurl.agent.enums.*;
import com.example.agenticurl.agent.repository.ApprovalRepository;
import com.example.agenticurl.agent.repository.ExecutionStepRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class GraphScheduler {

    private final Map<StepType, ExecutionStep> executors;
    private final ExecutionStepRepository stepRepository;
    private final ApprovalRepository approvalRepository;
    private final ObjectMapper objectMapper;

    public GraphScheduler(
            List<ExecutionStep> executors,
            ExecutionStepRepository stepRepository,
            ApprovalRepository approvalRepository,
            ObjectMapper objectMapper) {

        this.executors = executors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ExecutionStep::type,
                        step -> step
                ));

        this.stepRepository = stepRepository;
        this.approvalRepository = approvalRepository;
        this.objectMapper = objectMapper;
    }

    public SchedulerResult run(
            ExecutionContext context,
            BiConsumer<ExecutionStepEntity, StepResult> eventSink) {

        while (true) {
            List<ExecutionStepEntity> steps = loadCurrentSteps(context);

            restoreApprovedReviewSteps(context, steps);
            steps = loadCurrentSteps(context);

            Map<String, ExecutionStepEntity> byId = steps.stream()
                    .collect(Collectors.toMap(
                            ExecutionStepEntity::getStepId,
                            step -> step
                    ));

            Optional<ExecutionStepEntity> blocked = steps.stream()
                    .filter(step -> !step.isSuperseded())
                    .filter(step -> step.getStatus() == StepStatus.PENDING)
                    .filter(step -> dependenciesSucceeded(step, byId))
                    .filter(step -> step.getExecutionMode() == ExecutionMode.BLOCKED)
                    .findFirst();

            if (blocked.isPresent()) {
                return SchedulerResult.stopped(blocked.get().getStepId());
            }

            List<ExecutionStepEntity> runnable = steps.stream()
                    .filter(step -> !step.isSuperseded())
                    .filter(step -> step.getStatus() == StepStatus.PENDING)
                    .filter(step -> dependenciesSucceeded(step, byId))
                    .filter(step -> step.getExecutionMode() != ExecutionMode.BLOCKED)
                    .filter(step -> reviewBeforeSatisfied(context, step))
                    .toList();

            if (!runnable.isEmpty()) {
                SchedulerResult result = executeWave(context, runnable, eventSink);
                if (result != null) {
                    return result;
                }
                continue;
            }

            Optional<ExecutionStepEntity> awaitingReview = steps.stream()
                    .filter(step -> !step.isSuperseded())
                    .filter(step -> step.getStatus() == StepStatus.PENDING)
                    .filter(step -> dependenciesSucceeded(step, byId))
                    .filter(step -> step.getExecutionMode() == ExecutionMode.REVIEW_BEFORE_EXECUTION)
                    .filter(step -> !isApproved(context, step, ApprovalPhase.BEFORE_EXECUTION))
                    .findFirst();

            if (awaitingReview.isPresent()) {
                ExecutionStepEntity step = awaitingReview.get();
                step.markWaitingReview();
                stepRepository.save(step);
                return SchedulerResult.waiting(step.getStepId());
            }

            Optional<ExecutionStepEntity> waitingAfterExecution = steps.stream()
                    .filter(step -> !step.isSuperseded())
                    .filter(step -> step.getStatus() == StepStatus.WAITING_REVIEW)
                    .filter(step -> step.getExecutionMode() == ExecutionMode.REVIEW_AFTER_EXECUTION)
                    .findFirst();

            if (waitingAfterExecution.isPresent()) {
                return SchedulerResult.waiting(waitingAfterExecution.get().getStepId());
            }

            boolean complete = steps.stream()
                    .filter(step -> !step.isSuperseded())
                    .allMatch(step -> step.getStatus() == StepStatus.SUCCEEDED
                            || step.getStatus() == StepStatus.SKIPPED);

            if (complete) {
                return SchedulerResult.completedResult();
            }

            return SchedulerResult.stopped(null);
        }
    }

    private SchedulerResult executeWave(
            ExecutionContext context,
            List<ExecutionStepEntity> runnable,
            BiConsumer<ExecutionStepEntity, StepResult> eventSink) {

        runnable.forEach(ExecutionStepEntity::markRunning);
        stepRepository.saveAll(runnable);

        List<CompletableFuture<StepExecutionOutcome>> futures = runnable.stream()
                .map(record -> CompletableFuture.supplyAsync(
                        () -> executeWithPolicy(
                                record.toDefinition(),
                                record.getAttemptCount(),
                                context
                        )
                ))
                .toList();

        CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new)
        ).join();

        ExecutionStepEntity firstFailure = null;
        ExecutionStepEntity firstWaitingAfter = null;

        for (int index = 0; index < runnable.size(); index++) {
            ExecutionStepEntity record = runnable.get(index);
            StepExecutionOutcome outcome = futures.get(index).join();
            StepResult result = outcome.result();
            String dataJson = toJson(result.data());

            if (!result.success()) {
                record.markFailed(
                        outcome.attemptsUsed(),
                        result.summary(),
                        dataJson
                );

                if (firstFailure == null) {
                    firstFailure = record;
                }

                stepRepository.save(record);
                eventSink.accept(record, result);
                continue;
            }

            record.markSucceeded(
                    outcome.attemptsUsed(),
                    result.summary(),
                    dataJson,
                    result.fallbackUsed()
            );

            context.sharedState().put(record.getStepId(), result.data());

            if (record.getExecutionMode() == ExecutionMode.REVIEW_AFTER_EXECUTION
                    && !isApproved(context, record, ApprovalPhase.AFTER_EXECUTION)) {

                record.markWaitingReview();

                if (firstWaitingAfter == null) {
                    firstWaitingAfter = record;
                }
            }

            stepRepository.save(record);
            eventSink.accept(record, result);
        }

        if (firstFailure != null) {
            return new SchedulerResult(
                    false,
                    null,
                    firstFailure.getStepId(),
                    firstFailure.getFailureStrategy() == FailureStrategy.COMPENSATE,
                    firstFailure.getFailureStrategy() == FailureStrategy.REPLAN
            );
        }

        if (firstWaitingAfter != null) {
            return SchedulerResult.waiting(firstWaitingAfter.getStepId());
        }

        return null;
    }

    private void restoreApprovedReviewSteps(
            ExecutionContext context,
            List<ExecutionStepEntity> steps) {

        for (ExecutionStepEntity step : steps) {
            if (step.getStatus() != StepStatus.WAITING_REVIEW) {
                continue;
            }

            if (step.getExecutionMode() == ExecutionMode.REVIEW_BEFORE_EXECUTION
                    && isApproved(context, step, ApprovalPhase.BEFORE_EXECUTION)) {

                step.markPending();
                stepRepository.save(step);
            }

            if (step.getExecutionMode() == ExecutionMode.REVIEW_AFTER_EXECUTION
                    && isApproved(context, step, ApprovalPhase.AFTER_EXECUTION)) {

                step.approveCompletedStep();
                stepRepository.save(step);
            }
        }
    }

    private StepExecutionOutcome executeWithPolicy(
            StepDefinition step,
            int previousAttempts,
            ExecutionContext context) {

        ExecutionStep executor = Optional.ofNullable(executors.get(step.type()))
                .orElseThrow(() -> new IllegalStateException(
                        "No executor registered for " + step.type()
                ));

        int attemptsUsed = 0;
        int maximumAttempts = 1 + step.policy().maxRetries();
        StepResult result = null;

        for (int attempt = 1; attempt <= maximumAttempts; attempt++) {
            attemptsUsed++;

            context.sharedState().put(
                    "attempt:" + step.id(),
                    previousAttempts + attemptsUsed
            );

            result = executor.execute(context);

            if (result.success()) {
                return new StepExecutionOutcome(result, attemptsUsed);
            }

            FailureStrategy strategy = step.policy().failureStrategy();

            if (strategy != FailureStrategy.RETRY
                    && strategy != FailureStrategy.REPLAN) {
                break;
            }
        }

        return new StepExecutionOutcome(
                Objects.requireNonNull(result),
                attemptsUsed
        );
    }

    private boolean reviewBeforeSatisfied(
            ExecutionContext context,
            ExecutionStepEntity step) {

        return step.getExecutionMode() != ExecutionMode.REVIEW_BEFORE_EXECUTION
                || isApproved(context, step, ApprovalPhase.BEFORE_EXECUTION);
    }

    private boolean isApproved(
            ExecutionContext context,
            ExecutionStepEntity step,
            ApprovalPhase phase) {

        return approvalRepository
                .existsByWorkflowIdAndPlanVersionAndStepIdAndPhaseAndDecision(
                        context.workflowId(),
                        context.planVersion(),
                        step.getStepId(),
                        phase,
                        ReviewDecision.APPROVED
                );
    }

    private boolean dependenciesSucceeded(
            ExecutionStepEntity step,
            Map<String, ExecutionStepEntity> byId) {

        return step.getDependencies().stream()
                .allMatch(dependencyId -> {
                    ExecutionStepEntity dependency = byId.get(dependencyId);
                    return dependency != null
                            && dependency.getStatus() == StepStatus.SUCCEEDED;
                });
    }

    private List<ExecutionStepEntity> loadCurrentSteps(
            ExecutionContext context) {

        return stepRepository.findByWorkflowIdAndPlanVersionOrderById(
                context.workflowId(),
                context.planVersion()
        );
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not persist step result",
                    exception
            );
        }
    }

    private record StepExecutionOutcome(
            StepResult result,
            int attemptsUsed) {
    }

    public record SchedulerResult(
            boolean completed,
            String waitingStepId,
            String failedStepId,
            boolean compensationRequired,
            boolean replanningRecommended) {

        public static SchedulerResult waiting(String stepId) {
            return new SchedulerResult(false, stepId, null, false, false);
        }

        public static SchedulerResult completedResult() {
            return new SchedulerResult(true, null, null, false, false);
        }

        public static SchedulerResult stopped(String failedStepId) {
            return new SchedulerResult(false, null, failedStepId, false, false);
        }
    }
}
