package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.ExecutionEventEntity;
import com.example.agenticurl.agent.repository.ExecutionEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetWorkflowEventsService {
    private final ExecutionEventRepository repository;

    public GetWorkflowEventsService(ExecutionEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ExecutionEventEntity> get(UUID workflowId) {
        return repository.findByWorkflowIdOrderById(workflowId);
    }
}
