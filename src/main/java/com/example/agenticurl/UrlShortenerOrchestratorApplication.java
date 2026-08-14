package com.example.agenticurl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(
        info = @Info(
                title = "URL Shortener Orchestrator API",
                version = "1.0",
                description = "URL-shortener APIs and governed agentic engineering workflow APIs."
        )
)
public class UrlShortenerOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerOrchestratorApplication.class, args);
    }
}
