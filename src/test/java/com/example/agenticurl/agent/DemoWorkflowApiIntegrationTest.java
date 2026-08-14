package com.example.agenticurl.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demoapitest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoWorkflowApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void demoProfileAllowsExplicitFaultInjection() throws Exception {
        mockMvc.perform(
                        post("/api/demo/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "requirement": "Improve URL shortener documentation",
                                          "faults": {
                                            "buildFailuresBeforePass": 1,
                                            "securityScannerUnavailable": true,
                                            "deliveryFailure": false
                                          }
                                        }
                                        """)
                )
                .andExpect(status().isOk());
    }
}
