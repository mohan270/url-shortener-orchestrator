package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.execution.WorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClarifyWorkflowService {
    private final WorkflowEngine engine;

    public ClarifyWorkflowService(WorkflowEngine engine) {
        this.engine = engine;
    }

    @Transactional
    public WorkflowEntity clarify(UUID workflowId, String clarification) {
        return engine.clarify(workflowId, clarification);
    }
}
