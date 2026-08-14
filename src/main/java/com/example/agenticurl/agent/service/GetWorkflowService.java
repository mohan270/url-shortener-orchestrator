package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import com.example.agenticurl.agent.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetWorkflowService {
    private final WorkflowRepository repository;

    public GetWorkflowService(WorkflowRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WorkflowEntity get(UUID workflowId) {
        return repository.findById(workflowId)
                .orElseThrow(() -> new NoSuchElementException("Workflow not found"));
    }
}
