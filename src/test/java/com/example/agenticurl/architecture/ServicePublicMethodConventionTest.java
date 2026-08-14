package com.example.agenticurl.architecture;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicePublicMethodConventionTest {

    private static final List<Class<?>> SERVICE_CLASSES = List.of(
            com.example.agenticurl.url.service.CreateShortUrlService.class,
            com.example.agenticurl.url.service.ResolveShortUrlService.class,
            com.example.agenticurl.url.service.GetUrlAnalyticsService.class,
            com.example.agenticurl.agent.service.StartWorkflowService.class,
            com.example.agenticurl.agent.service.StartDemoWorkflowService.class,
            com.example.agenticurl.agent.service.ClarifyWorkflowService.class,
            com.example.agenticurl.agent.service.ReviewWorkflowStepService.class,
            com.example.agenticurl.agent.service.ReplanWorkflowService.class,
            com.example.agenticurl.agent.service.GetWorkflowService.class,
            com.example.agenticurl.agent.service.GetWorkflowEventsService.class,
            com.example.agenticurl.agent.service.GetWorkflowStepsService.class,
            com.example.agenticurl.agent.service.GetWorkflowApprovalsService.class,
            com.example.agenticurl.agent.service.GetWorkflowMetricsService.class
    );

    @Test
    void eachServiceHasExactlyOneDeclaredPublicBusinessMethod() {
        for (Class<?> serviceClass : SERVICE_CLASSES) {
            long publicMethodCount = List.of(serviceClass.getDeclaredMethods()).stream()
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .filter(method -> !method.isSynthetic())
                    .count();

            assertEquals(
                    1,
                    publicMethodCount,
                    () -> serviceClass.getSimpleName()
                            + " should declare exactly one public method"
            );
        }
    }
}
