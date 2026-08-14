package com.example.agenticurl.agent.service;

import com.example.agenticurl.agent.entity.ApprovalEntity;
import com.example.agenticurl.agent.repository.ApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetWorkflowApprovalsService {
    private final ApprovalRepository repository;

    public GetWorkflowApprovalsService(ApprovalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ApprovalEntity> get(UUID workflowId) {
        return repository.findByWorkflowIdOrderByCreatedAtAsc(workflowId);
    }
}
