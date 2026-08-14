package com.example.agenticurl.agent.repository;

import com.example.agenticurl.agent.entity.ExecutionStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionStepRepository extends JpaRepository<ExecutionStepEntity, Long> {

    List<ExecutionStepEntity> findByWorkflowIdAndPlanVersionOrderById(
            UUID workflowId,
            int planVersion
    );

    Optional<ExecutionStepEntity> findByWorkflowIdAndPlanVersionAndStepId(
            UUID workflowId,
            int planVersion,
            String stepId
    );

    boolean existsByWorkflowIdAndPlanVersion(
            UUID workflowId,
            int planVersion
    );

    List<ExecutionStepEntity> findByWorkflowIdOrderByPlanVersionAscIdAsc(
            UUID workflowId
    );
}
