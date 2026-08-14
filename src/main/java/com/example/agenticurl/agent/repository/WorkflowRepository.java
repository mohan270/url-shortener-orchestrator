package com.example.agenticurl.agent.repository;

import com.example.agenticurl.agent.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {
}
