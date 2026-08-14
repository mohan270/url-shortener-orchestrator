package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.enums.ReviewDecision;
import com.example.agenticurl.agent.execution.WorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewWorkflowStepService {
    private final WorkflowEngine engine;

    public ReviewWorkflowStepService(WorkflowEngine engine) {
        this.engine = engine;
    }

    @Transactional
    public WorkflowEntity review(
            UUID workflowId,
            String stepId,
            ReviewDecision decision,
            String comment) {
        return engine.review(workflowId, stepId, decision, comment);
    }
}
