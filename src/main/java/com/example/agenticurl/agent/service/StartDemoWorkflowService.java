package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.domain.FaultInjectionOptions;
import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.execution.WorkflowEngine;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("demo")
public class StartDemoWorkflowService {

    private final WorkflowEngine engine;

    public StartDemoWorkflowService(WorkflowEngine engine) {
        this.engine = engine;
    }

    @Transactional
    public WorkflowEntity start(
            String requirement,
            FaultInjectionOptions faults) {

        return engine.start(
                requirement,
                faults == null
                        ? FaultInjectionOptions.none()
                        : faults
        );
    }
}
