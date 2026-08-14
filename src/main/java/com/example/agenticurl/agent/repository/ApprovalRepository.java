package com.example.agenticurl.agent.repository;

import com.example.agenticurl.agent.entity.ApprovalEntity;
import com.example.agenticurl.agent.enums.ApprovalPhase;
import com.example.agenticurl.agent.enums.ReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {

    boolean existsByWorkflowIdAndPlanVersionAndStepIdAndPhaseAndDecision(
            UUID workflowId,
            int planVersion,
            String stepId,
            ApprovalPhase phase,
            ReviewDecision decision
    );

    boolean existsByWorkflowIdAndPlanVersionAndStepIdAndPhase(
            UUID workflowId,
            int planVersion,
            String stepId,
            ApprovalPhase phase
    );

    List<ApprovalEntity> findByWorkflowIdOrderByCreatedAtAsc(UUID workflowId);
}
