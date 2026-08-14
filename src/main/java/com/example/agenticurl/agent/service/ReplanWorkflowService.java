package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.execution.WorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReplanWorkflowService {
    private final WorkflowEngine engine;

    public ReplanWorkflowService(WorkflowEngine engine) {
        this.engine = engine;
    }

    @Transactional
    public WorkflowEntity replan(UUID workflowId, String newRequirement) {
        return engine.replan(workflowId, newRequirement);
    }
}
