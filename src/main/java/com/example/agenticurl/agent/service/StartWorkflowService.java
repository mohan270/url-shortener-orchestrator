package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.execution.WorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartWorkflowService {

    private final WorkflowEngine engine;

    public StartWorkflowService(WorkflowEngine engine) {
        this.engine = engine;
    }

    @Transactional
    public WorkflowEntity start(String requirement) {
        return engine.start(
                requirement,
                FaultInjectionOptions.none()
        );
    }
}
