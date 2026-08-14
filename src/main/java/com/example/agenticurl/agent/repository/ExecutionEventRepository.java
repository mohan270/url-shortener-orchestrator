package com.example.agenticurl.agent.repository;

import com.example.agenticurl.agent.entity.ExecutionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExecutionEventRepository extends JpaRepository<ExecutionEventEntity, Long> {
    List<ExecutionEventEntity> findByWorkflowIdOrderById(UUID workflowId);
}
