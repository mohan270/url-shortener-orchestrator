package com.example.agenticurl.agent.controller;

import com.example.agenticurl.agent.dto.DemoWorkflowRequest;
import com.example.agenticurl.agent.dto.WorkflowView;
import com.example.agenticurl.agent.service.StartDemoWorkflowService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/workflows")
@Profile("demo")
@Hidden
public class DemoWorkflowController {

    private final StartDemoWorkflowService startDemoWorkflowService;

    public DemoWorkflowController(
            StartDemoWorkflowService startDemoWorkflowService) {

        this.startDemoWorkflowService = startDemoWorkflowService;
    }

    @PostMapping
    public WorkflowView start(
            @Valid @RequestBody DemoWorkflowRequest request) {

        return WorkflowView.from(
                startDemoWorkflowService.start(
                        request.requirement(),
                        request.faults() == null
                                ? null
                                : request.faults().toOptions()
                )
        );
    }
}
