package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import com.example.agenticurl.agent.repository.ExecutionStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetWorkflowStepsService {
    private final ExecutionStepRepository repository;

    public GetWorkflowStepsService(ExecutionStepRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ExecutionStepEntity> get(UUID workflowId) {
        return repository.findByWorkflowIdOrderByPlanVersionAscIdAsc(workflowId);
    }
}
